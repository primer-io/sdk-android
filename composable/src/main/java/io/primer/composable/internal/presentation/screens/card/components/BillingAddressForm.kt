package io.primer.composable.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

@Composable
internal fun BillingAddressForm(
    billingInputFields: List<PrimerInputElementType>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        if (billingInputFields.contains(PrimerInputElementType.COUNTRY_CODE)) {
            Input(
                type = PrimerInputElementType.COUNTRY_CODE,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // First name | Last name
        val hasFirstName = billingInputFields.contains(PrimerInputElementType.FIRST_NAME)
        val hasLastName = billingInputFields.contains(PrimerInputElementType.LAST_NAME)
        
        if (hasFirstName || hasLastName) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasFirstName) {
                    Input(
                        type = PrimerInputElementType.FIRST_NAME,
                        modifier = if (hasLastName) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }
                if (hasLastName) {
                    Input(
                        type = PrimerInputElementType.LAST_NAME,
                        modifier = if (hasFirstName) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Address line 1
        if (billingInputFields.contains(PrimerInputElementType.ADDRESS_LINE_1)) {
            Input(
                type = PrimerInputElementType.ADDRESS_LINE_1,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Address line 2 (optional)
        if (billingInputFields.contains(PrimerInputElementType.ADDRESS_LINE_2)) {
            Input(
                type = PrimerInputElementType.ADDRESS_LINE_2,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Postal code | City
        val hasPostalCode = billingInputFields.contains(PrimerInputElementType.POSTAL_CODE)
        val hasCity = billingInputFields.contains(PrimerInputElementType.CITY)
        
        if (hasPostalCode || hasCity) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasPostalCode) {
                    Input(
                        type = PrimerInputElementType.POSTAL_CODE,
                        modifier = if (hasCity) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }
                if (hasCity) {
                    Input(
                        type = PrimerInputElementType.CITY,
                        modifier = if (hasPostalCode) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // State / Region / County
        if (billingInputFields.contains(PrimerInputElementType.STATE)) {
            Input(
                type = PrimerInputElementType.STATE,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
