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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Card number
        Input(
            type = PrimerInputElementType.CARD_NUMBER,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Expiry date | CVV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Input(
                type = PrimerInputElementType.EXPIRY_DATE,
                modifier = Modifier.weight(1f)
            )
            Input(
                type = PrimerInputElementType.CVV,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Cardholder name
        Input(
            type = PrimerInputElementType.CARDHOLDER_NAME,
            modifier = Modifier.fillMaxWidth()
        )
    }
}