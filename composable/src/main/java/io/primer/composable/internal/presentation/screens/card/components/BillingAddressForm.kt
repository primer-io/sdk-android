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
internal fun BillingAddressForm(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Input(
            type = PrimerInputElementType.COUNTRY_CODE,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Input(
                type = PrimerInputElementType.FIRST_NAME,
                modifier = Modifier.weight(1f)
            )
            Input(
                type = PrimerInputElementType.LAST_NAME,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        Input(
            type = PrimerInputElementType.ADDRESS_LINE_1,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Input(
            type = PrimerInputElementType.ADDRESS_LINE_2,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Input(
                type = PrimerInputElementType.POSTAL_CODE,
                modifier = Modifier.weight(1f)
            )
            Input(
                type = PrimerInputElementType.CITY,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        Input(
            type = PrimerInputElementType.STATE,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
