package io.primer.composable.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

@Composable
internal fun CardDetailsForm(
    modifier: Modifier = Modifier,
    cardInputFields: List<PrimerInputElementType>,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (cardInputFields.contains(PrimerInputElementType.CARD_NUMBER)) {
            Input(
                type = PrimerInputElementType.CARD_NUMBER,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (cardInputFields.any { it == PrimerInputElementType.EXPIRY_DATE || it == PrimerInputElementType.CVV || it == PrimerInputElementType.CARDHOLDER_NAME }) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        val hasExpiry = cardInputFields.contains(PrimerInputElementType.EXPIRY_DATE)
        val hasCvv = cardInputFields.contains(PrimerInputElementType.CVV)
        
        if (hasExpiry || hasCvv) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasExpiry) {
                    Input(
                        type = PrimerInputElementType.EXPIRY_DATE,
                        modifier = if (hasCvv) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }
                if (hasCvv) {
                    Input(
                        type = PrimerInputElementType.CVV,
                        modifier = if (hasExpiry) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }
            }
            
            if (cardInputFields.contains(PrimerInputElementType.CARDHOLDER_NAME)) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (cardInputFields.contains(PrimerInputElementType.CARDHOLDER_NAME)) {
            Input(
                type = PrimerInputElementType.CARDHOLDER_NAME,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
