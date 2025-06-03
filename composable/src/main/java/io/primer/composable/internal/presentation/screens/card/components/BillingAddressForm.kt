package io.primer.composable.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.BillingAddressForm(
    modifier: Modifier = Modifier
) {

    val state by state.collectAsState()
    val billingInputFields = state.billingFields
    if (billingInputFields.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Text(
            text = "BILLING ADDRESS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Input(
            modifier = Modifier.fillMaxWidth(),
            type = PrimerInputElementType.COUNTRY_CODE
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Input(
                modifier = Modifier.weight(1f),
                type = PrimerInputElementType.FIRST_NAME
            )
            Input(
                modifier = Modifier.weight(1f),
                type = PrimerInputElementType.LAST_NAME
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Input(
            modifier = Modifier.fillMaxWidth(),
            type = PrimerInputElementType.ADDRESS_LINE_1
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Input(
            modifier = Modifier.fillMaxWidth(),
            type = PrimerInputElementType.ADDRESS_LINE_2
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Input(
                modifier = Modifier.weight(1f),
                type = PrimerInputElementType.POSTAL_CODE
            )
            Input(
                modifier = Modifier.weight(1f),
                type = PrimerInputElementType.CITY
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Input(
            modifier = Modifier.fillMaxWidth(),
            type = PrimerInputElementType.STATE
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
