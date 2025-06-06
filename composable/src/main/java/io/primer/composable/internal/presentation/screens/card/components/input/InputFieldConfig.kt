package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.cardShared.CardNumberFormatter
import io.primer.composable.internal.presentation.screens.card.components.input.transformations.CardNumberVisualTransformation
import io.primer.composable.internal.presentation.screens.card.components.input.transformations.ExpiryDateVisualTransformation

internal data class InputFieldConfig(
    val label: String,
    val placeholder: String,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val visualTransformation: VisualTransformation = VisualTransformation.None,
    val maxLength: Int? = null,
    val allowedChars: String? = null
)

internal object InputFieldConfigurations {
    
    fun getConfig(
        type: PrimerInputElementType,
        currentInputs: Map<PrimerInputElementType, String> = emptyMap()
    ): InputFieldConfig = when (type) {
        PrimerInputElementType.CARD_NUMBER -> {
            val cardNumber = currentInputs[PrimerInputElementType.CARD_NUMBER] ?: ""
            val formatter = CardNumberFormatter.fromString(cardNumber)
            InputFieldConfig(
                label = "Card Number",
                placeholder = "1234 5678 9012 3456",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CardNumberVisualTransformation(),
                maxLength = formatter.getMaxLength(),
                allowedChars = "0123456789"
            )
        }
        
        PrimerInputElementType.CARDHOLDER_NAME -> InputFieldConfig(
            label = "Cardholder Name",
            placeholder = "John Doe"
        )
        
        PrimerInputElementType.EXPIRY_DATE -> InputFieldConfig(
            label = "Expiry Date",
            placeholder = "MM/YY",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ExpiryDateVisualTransformation(),
            maxLength = 5,
            allowedChars = "0123456789/"
        )
        
        PrimerInputElementType.CVV -> {
            val cardNumber = currentInputs[PrimerInputElementType.CARD_NUMBER] ?: ""
            val formatter = CardNumberFormatter.fromString(cardNumber)
            val cvvLength = formatter.getCvvLength()
            
            InputFieldConfig(
                label = "CVV",
                placeholder = "1".repeat(cvvLength),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                maxLength = cvvLength,
                allowedChars = "0123456789"
            )
        }
        
        PrimerInputElementType.ALL -> InputFieldConfig(
            label = "All Fields",
            placeholder = "Select all fields"
        )
        
        PrimerInputElementType.POSTAL_CODE -> InputFieldConfig(
            label = "Postal Code",
            placeholder = "12345",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        PrimerInputElementType.COUNTRY_CODE -> InputFieldConfig(
            label = "Country Code",
            placeholder = "US"
        )
        
        PrimerInputElementType.CITY -> InputFieldConfig(
            label = "City",
            placeholder = "New York"
        )
        
        PrimerInputElementType.STATE -> InputFieldConfig(
            label = "State",
            placeholder = "NY"
        )
        
        PrimerInputElementType.ADDRESS_LINE_1 -> InputFieldConfig(
            label = "Address Line 1",
            placeholder = "123 Main Street"
        )
        
        PrimerInputElementType.ADDRESS_LINE_2 -> InputFieldConfig(
            label = "Address Line 2",
            placeholder = "Apt 4B"
        )
        
        PrimerInputElementType.PHONE_NUMBER -> InputFieldConfig(
            label = "Phone Number",
            placeholder = "+1 (555) 123-4567",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        
        PrimerInputElementType.FIRST_NAME -> InputFieldConfig(
            label = "First Name",
            placeholder = "John"
        )
        
        PrimerInputElementType.LAST_NAME -> InputFieldConfig(
            label = "Last Name",
            placeholder = "Doe"
        )
        
        PrimerInputElementType.RETAIL_OUTLET -> InputFieldConfig(
            label = "Retail Outlet",
            placeholder = "Select outlet"
        )
        
        PrimerInputElementType.OTP_CODE -> InputFieldConfig(
            label = "OTP Code",
            placeholder = "123456",
            allowedChars = "0123456789",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}
