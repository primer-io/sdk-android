package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.cardShared.CardNumberFormatter
import io.primer.composable.internal.presentation.screens.card.components.input.transformations.CardNumberVisualTransformation
import io.primer.composable.internal.presentation.screens.card.components.input.transformations.ExpiryDateVisualTransformation
import io.primer.composable.scope.CardFormScope

internal data class InputFieldConfig(
    val label: String,
    val placeholder: String,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val visualTransformation: VisualTransformation = VisualTransformation.None,
    val maxLength: Int? = null,
    val allowedChars: String? = null,
) {
    companion object {
        val CARDHOLDER_NAME = InputFieldConfig(
            label = "Cardholder Name",
            placeholder = "John Doe",
        )
        
        val EXPIRY_DATE = InputFieldConfig(
            label = "Expiry Date",
            placeholder = "MM/YYYY",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ExpiryDateVisualTransformation(),
            maxLength = 6,
            allowedChars = "0123456789",
        )
        
        val POSTAL_CODE = InputFieldConfig(
            label = "Postal Code",
            placeholder = "12345",
        )
        
        val COUNTRY_CODE = InputFieldConfig(
            label = "Country Code",
            placeholder = "US",
        )
        
        val CITY = InputFieldConfig(
            label = "City",
            placeholder = "New York",
        )
        
        val STATE = InputFieldConfig(
            label = "State",
            placeholder = "NY",
        )
        
        val ADDRESS_LINE_1 = InputFieldConfig(
            label = "Address Line 1",
            placeholder = "123 Main Street",
        )
        
        val ADDRESS_LINE_2 = InputFieldConfig(
            label = "Address Line 2",
            placeholder = "Apt 4B",
        )
        
        val PHONE_NUMBER = InputFieldConfig(
            label = "Phone Number",
            placeholder = "+1 (555) 123-4567",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        
        val FIRST_NAME = InputFieldConfig(
            label = "First Name",
            placeholder = "John",
        )
        
        val LAST_NAME = InputFieldConfig(
            label = "Last Name",
            placeholder = "Doe",
        )
        
        val RETAIL_OUTLET = InputFieldConfig(
            label = "Retail Outlet",
            placeholder = "Select outlet",
        )
        
        val OTP_CODE = InputFieldConfig(
            label = "OTP Code",
            placeholder = "123456",
            allowedChars = "0123456789",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}

@Composable
private fun CardFormScope.Input(
    modifier: Modifier = Modifier,
    type: PrimerInputElementType,
    config: InputFieldConfig,
    onValueChange: (String) -> Unit,
) {
    val state by state.collectAsState()

    // Check if this field should be shown
    val isFieldRequired = type in state.cardFields || type in state.billingFields
    if (!isFieldRequired) return

    val value = state.inputFields[type] ?: ""
    val error = state.fieldErrors.find { it.inputElementType == type }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(config.label) },
        placeholder = { Text(config.placeholder) },
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
        visualTransformation = config.visualTransformation,
        keyboardOptions = config.keyboardOptions,
    )
}

@Composable
internal fun CardFormScope.CardNumberInput(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()
    val cardNumber = state.inputFields[PrimerInputElementType.CARD_NUMBER] ?: ""
    val formatter = CardNumberFormatter.fromString(cardNumber)
    val config = InputFieldConfig(
        label = "Card Number",
        placeholder = "1234 5678 9012 3456",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = CardNumberVisualTransformation(),
        maxLength = formatter.getMaxLength(),
        allowedChars = "0123456789",
    )
    
    Input(
        modifier = modifier,
        type = PrimerInputElementType.CARD_NUMBER,
        config = config,
        onValueChange = ::updateCardNumber
    )
}

@Composable
internal fun CardFormScope.CvvInput(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()
    val cardNumber = state.inputFields[PrimerInputElementType.CARD_NUMBER] ?: ""
    val formatter = CardNumberFormatter.fromString(cardNumber)
    val cvvLength = formatter.getCvvLength()
    val config = InputFieldConfig(
        label = "CVV",
        placeholder = "1".repeat(cvvLength),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxLength = cvvLength,
        allowedChars = "0123456789",
    )
    
    Input(
        modifier = modifier,
        type = PrimerInputElementType.CVV,
        config = config,
        onValueChange = ::updateCvv
    )
}

@Composable
internal fun CardFormScope.ExpiryDateInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.EXPIRY_DATE, InputFieldConfig.EXPIRY_DATE, ::updateExpiryDate)

@Composable
internal fun CardFormScope.CardholderNameInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.CARDHOLDER_NAME, InputFieldConfig.CARDHOLDER_NAME, ::updateCardholderName)

@Composable
internal fun CardFormScope.PostalCodeInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.POSTAL_CODE, InputFieldConfig.POSTAL_CODE, ::updatePostalCode)

@Composable
internal fun CardFormScope.CountryCodeInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.COUNTRY_CODE, InputFieldConfig.COUNTRY_CODE, ::updateCountryCode)

@Composable
internal fun CardFormScope.CityInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.CITY, InputFieldConfig.CITY, ::updateCity)

@Composable
internal fun CardFormScope.StateInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.STATE, InputFieldConfig.STATE, ::updateState)

@Composable
internal fun CardFormScope.AddressLine1Input(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.ADDRESS_LINE_1, InputFieldConfig.ADDRESS_LINE_1, ::updateAddressLine1)

@Composable
internal fun CardFormScope.AddressLine2Input(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.ADDRESS_LINE_2, InputFieldConfig.ADDRESS_LINE_2, ::updateAddressLine2)

@Composable
internal fun CardFormScope.PhoneNumberInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.PHONE_NUMBER, InputFieldConfig.PHONE_NUMBER, ::updatePhoneNumber)

@Composable
internal fun CardFormScope.FirstNameInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.FIRST_NAME, InputFieldConfig.FIRST_NAME, ::updateFirstName)

@Composable
internal fun CardFormScope.LastNameInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.LAST_NAME, InputFieldConfig.LAST_NAME, ::updateLastName)

@Composable
internal fun CardFormScope.RetailOutletInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.RETAIL_OUTLET, InputFieldConfig.RETAIL_OUTLET, ::updateRetailOutlet)

@Composable
internal fun CardFormScope.OtpCodeInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.OTP_CODE, InputFieldConfig.OTP_CODE, ::updateOtpCode)
