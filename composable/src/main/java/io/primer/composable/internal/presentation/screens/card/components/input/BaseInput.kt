package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.BaseInput(
    modifier: Modifier = Modifier,
    type: PrimerInputElementType,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val state by state.collectAsState()

    // Check if this field should be shown
    val isFieldRequired = type in state.cardFields || type in state.billingFields
    if (!isFieldRequired) return

    val error = state.fieldErrors.find { it.inputElementType == type }

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
        onValueChange = onValueChange,
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
    )
}
