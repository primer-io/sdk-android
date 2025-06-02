package io.primer.composable.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

@Composable
internal fun Input(
    type: PrimerInputElementType,
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf("") }

    val (label, placeholder) = when (type) {
        PrimerInputElementType.CARD_NUMBER -> "Card Number" to "1234 5678 9012 3456"
        PrimerInputElementType.CARDHOLDER_NAME -> "Cardholder Name" to "John Doe"
        PrimerInputElementType.EXPIRY_DATE -> "Expiry Date" to "MM/YY"
        PrimerInputElementType.CVV -> "CVV" to "123"
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

    OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}
