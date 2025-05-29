package io.primer.components.clean.internal.presentation.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.components.clean.internal.domain.usecases.ValidateCardUseCase

/**
 * Clean Architecture Card Component.
 * Receives events and state through the ViewModel following clean architecture principles.
 */
@Composable
internal fun CardComponent(
    viewModel: CardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        
        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
        
        // Card Number Field
        OutlinedTextField(
            value = uiState.card.number,
            onValueChange = { viewModel.handleEvent(CardUiEvent.CardNumberChanged(it)) },
            label = { Text("Card Number") },
            placeholder = { Text("1234 1234 1234 1234") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.contains(
                ValidateCardUseCase.ValidationResult.ValidationError.INVALID_CARD_NUMBER
            ),
            supportingText = {
                if (uiState.validationErrors.contains(
                        ValidateCardUseCase.ValidationResult.ValidationError.INVALID_CARD_NUMBER
                    )) {
                    Text("Invalid card number", color = MaterialTheme.colorScheme.error)
                }
            }
        )
        
        // Expiry and CVV Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Expiry Field (simplified - in real implementation you'd parse MM/YY)
            OutlinedTextField(
                value = if (uiState.card.expiryMonth > 0 && uiState.card.expiryYear > 0) {
                    String.format("%02d/%02d", uiState.card.expiryMonth, uiState.card.expiryYear % 100)
                } else "",
                onValueChange = { input ->
                    // Parse MM/YY format
                    val parts = input.split("/")
                    if (parts.size == 2) {
                        val month = parts[0].toIntOrNull() ?: 0
                        val year = parts[1].toIntOrNull()?.let { 
                            if (it < 100) 2000 + it else it 
                        } ?: 0
                        viewModel.handleEvent(CardUiEvent.ExpiryChanged(month, year))
                    }
                },
                label = { Text("Expiry (MM/YY)") },
                placeholder = { Text("12/25") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                isError = uiState.validationErrors.any { 
                    it == ValidateCardUseCase.ValidationResult.ValidationError.INVALID_EXPIRY_DATE ||
                    it == ValidateCardUseCase.ValidationResult.ValidationError.CARD_EXPIRED
                },
                supportingText = {
                    if (uiState.validationErrors.contains(
                            ValidateCardUseCase.ValidationResult.ValidationError.INVALID_EXPIRY_DATE
                        )) {
                        Text("Invalid expiry date", color = MaterialTheme.colorScheme.error)
                    } else if (uiState.validationErrors.contains(
                            ValidateCardUseCase.ValidationResult.ValidationError.CARD_EXPIRED
                        )) {
                        Text("Card expired", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            
            // CVV Field
            OutlinedTextField(
                value = uiState.card.cvv,
                onValueChange = { viewModel.handleEvent(CardUiEvent.CvvChanged(it)) },
                label = { Text("CVV") },
                placeholder = { Text("123") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                isError = uiState.validationErrors.contains(
                    ValidateCardUseCase.ValidationResult.ValidationError.INVALID_CVV
                ),
                supportingText = {
                    if (uiState.validationErrors.contains(
                            ValidateCardUseCase.ValidationResult.ValidationError.INVALID_CVV
                        )) {
                        Text("Invalid CVV", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
        
        // Cardholder Name Field
        OutlinedTextField(
            value = uiState.card.holderName,
            onValueChange = { viewModel.handleEvent(CardUiEvent.HolderNameChanged(it)) },
            label = { Text("Cardholder Name") },
            placeholder = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.contains(
                ValidateCardUseCase.ValidationResult.ValidationError.INVALID_HOLDER_NAME
            ),
            supportingText = {
                if (uiState.validationErrors.contains(
                        ValidateCardUseCase.ValidationResult.ValidationError.INVALID_HOLDER_NAME
                    )) {
                    Text("Cardholder name is required", color = MaterialTheme.colorScheme.error)
                }
            }
        )
        
        // Submit Error
        uiState.submitError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        // Submit Button
        Button(
            onClick = { viewModel.handleEvent(CardUiEvent.SubmitCard) },
            enabled = uiState.canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    color = Color.White
                )
            }
            Text(if (uiState.isSubmitting) "Processing..." else "Pay Now")
        }
    }
}
