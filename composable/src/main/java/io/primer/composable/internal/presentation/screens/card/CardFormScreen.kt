package io.primer.composable.internal.presentation.screens.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.primer.composable.internal.presentation.screens.card.components.BillingAddressForm
import io.primer.composable.internal.presentation.screens.card.components.CardDetailsForm
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.CardFormScreen(
    modifier: Modifier = Modifier,
    submitButton: (@Composable () -> Unit)? = { SubmitButton(text = "Submit") }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Card Details Section
        Text(
            text = "CARD DETAILS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        CardDetailsForm()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Billing Address Section
        Text(
            text = "BILLING ADDRESS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        BillingAddressForm()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        submitButton?.invoke()
    }
}

@Composable
internal fun CardFormScope.SubmitButton(
    modifier: Modifier = Modifier,
    text: String
) {
    Button(
        onClick = { submit() },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
