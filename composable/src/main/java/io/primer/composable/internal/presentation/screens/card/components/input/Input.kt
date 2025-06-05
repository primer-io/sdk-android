package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.cardShared.CardNumberFormatter
import io.primer.composable.internal.presentation.screens.card.components.input.transformations.CardNumberVisualTransformation
import io.primer.composable.internal.presentation.screens.card.components.input.transformations.ExpiryDateVisualTransformation
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.Input(
    modifier: Modifier = Modifier,
    type: PrimerInputElementType
) {

    val state by state.collectAsState()
    val value = state.inputFields[type] ?: ""

    // Check if this field should be shown
    val isFieldRequired = type in state.cardFields || type in state.billingFields
    if (!isFieldRequired) return

    val error = state.fieldErrors.find { it.inputElementType == type }

    val (label, placeholder) = when (type) {
        PrimerInputElementType.CARD_NUMBER -> "Card Number" to "1234 5678 9012 3456"
        PrimerInputElementType.CARDHOLDER_NAME -> "Cardholder Name" to "John Doe"
        PrimerInputElementType.EXPIRY_DATE -> "Expiry Date" to "MM/YY"
        PrimerInputElementType.CVV -> {
            // Dynamic CVV placeholder based on card type
            val cardNumber = state.inputFields[PrimerInputElementType.CARD_NUMBER] ?: ""
            val formatter = CardNumberFormatter.fromString(cardNumber)
            val cvvLength = formatter.getCvvLength()
            "CVV" to "1".repeat(cvvLength)
        }
        PrimerInputElementType.ALL -> "All Fields" to "Select all fields"
        PrimerInputElementType.POSTAL_CODE -> "Postal Code" to "12345"
        PrimerInputElementType.COUNTRY_CODE -> "Country Code" to "US"
        PrimerInputElementType.CITY -> "City" to "New York"
        PrimerInputElementType.STATE -> "State" to "NY"
        PrimerInputElementType.ADDRESS_LINE_1 -> "Address Line 1" to "123 Main Street"
        PrimerInputElementType.ADDRESS_LINE_2 -> "Address Line 2" to "Apt 4B"
        PrimerInputElementType.PHONE_NUMBER -> "Phone Number" to "+1 (555) 123-4567"
        PrimerInputElementType.FIRST_NAME -> "First Name" to "John"
        PrimerInputElementType.LAST_NAME -> "Last Name" to "Doe"
        PrimerInputElementType.RETAIL_OUTLET -> "Retail Outlet" to "Select outlet"
        PrimerInputElementType.OTP_CODE -> "OTP Code" to "123456"
    }

    // Visual transformation and keyboard options based on input type
    val visualTransformation = remember(type, value) {
        when (type) {
            PrimerInputElementType.CARD_NUMBER -> CardNumberVisualTransformation()
            PrimerInputElementType.EXPIRY_DATE -> ExpiryDateVisualTransformation()
            else -> VisualTransformation.None
        }
    }

    val keyboardOptions = when (type) {
        PrimerInputElementType.CARD_NUMBER,
        PrimerInputElementType.CVV,
        PrimerInputElementType.EXPIRY_DATE,
        PrimerInputElementType.POSTAL_CODE,
        PrimerInputElementType.OTP_CODE -> KeyboardOptions(keyboardType = KeyboardType.Number)
        PrimerInputElementType.PHONE_NUMBER -> KeyboardOptions(keyboardType = KeyboardType.Phone)
        else -> KeyboardOptions.Default
    }

    // Process value changes with formatters
    val processedOnValueChange: (String) -> Unit = remember(type, state) {
        when (type) {
            PrimerInputElementType.CARD_NUMBER -> { newValue ->
                // Remove non-digits and limit to max card length
                val formatter = CardNumberFormatter.fromString(newValue)
                val sanitized = formatter.getValue()
                if (sanitized.length <= formatter.getMaxLength()) {
                    updateInput(type to sanitized)
                }
            }
            PrimerInputElementType.EXPIRY_DATE -> { newValue ->
                // Allow only digits and forward slash, limit to MM/YY format
                val cleaned = newValue.filter { it.isDigit() || it == '/' }
                if (cleaned.length <= 5) { // MM/YY = 5 chars max
                    updateInput(type to cleaned)
                }
            }
            PrimerInputElementType.CVV -> { newValue ->
                // Get dynamic CVV length based on card number
                val cardNumber = state.inputFields[PrimerInputElementType.CARD_NUMBER] ?: ""
                val formatter = CardNumberFormatter.fromString(cardNumber)
                val maxCvvLength = formatter.getCvvLength()
                
                // Allow only digits and limit to max CVV length
                val cleaned = newValue.filter { it.isDigit() }
                if (cleaned.length <= maxCvvLength) {
                    updateInput(type to cleaned)
                }
            }
            else -> { newValue -> updateInput(type to newValue) }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = processedOnValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        supportingText = {
            error?.let {
                Text(
                    text = it.description,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
    )
}
