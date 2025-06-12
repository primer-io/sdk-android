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

private object InputConfigs {
    fun label(type: PrimerInputElementType): String = when (type) {
        PrimerInputElementType.CARDHOLDER_NAME -> "Cardholder Name"
        PrimerInputElementType.EXPIRY_DATE -> "Expiry Date"
        PrimerInputElementType.POSTAL_CODE -> "Postal Code"
        PrimerInputElementType.COUNTRY_CODE -> "Country Code"
        PrimerInputElementType.CITY -> "City"
        PrimerInputElementType.STATE -> "State"
        PrimerInputElementType.ADDRESS_LINE_1 -> "Address Line 1"
        PrimerInputElementType.ADDRESS_LINE_2 -> "Address Line 2"
        PrimerInputElementType.PHONE_NUMBER -> "Phone Number"
        PrimerInputElementType.FIRST_NAME -> "First Name"
        PrimerInputElementType.LAST_NAME -> "Last Name"
        PrimerInputElementType.RETAIL_OUTLET -> "Retail Outlet"
        PrimerInputElementType.OTP_CODE -> "OTP Code"
        else -> type.field
    }

    fun placeholder(type: PrimerInputElementType): String = when (type) {
        PrimerInputElementType.CARDHOLDER_NAME -> "John Doe"
        PrimerInputElementType.EXPIRY_DATE -> "MM/YYYY"
        PrimerInputElementType.POSTAL_CODE -> "12345"
        PrimerInputElementType.COUNTRY_CODE -> "US"
        PrimerInputElementType.CITY -> "New York"
        PrimerInputElementType.STATE -> "NY"
        PrimerInputElementType.ADDRESS_LINE_1 -> "123 Main Street"
        PrimerInputElementType.ADDRESS_LINE_2 -> "Apt 4B"
        PrimerInputElementType.PHONE_NUMBER -> "+1 (555) 123-4567"
        PrimerInputElementType.FIRST_NAME -> "John"
        PrimerInputElementType.LAST_NAME -> "Doe"
        PrimerInputElementType.RETAIL_OUTLET -> "Select outlet"
        PrimerInputElementType.OTP_CODE -> "123456"
        else -> ""
    }

    fun keyboardOptions(type: PrimerInputElementType): KeyboardOptions = when (type) {
        PrimerInputElementType.EXPIRY_DATE, PrimerInputElementType.OTP_CODE ->
            KeyboardOptions(keyboardType = KeyboardType.Number)
        PrimerInputElementType.PHONE_NUMBER ->
            KeyboardOptions(keyboardType = KeyboardType.Phone)
        PrimerInputElementType.POSTAL_CODE ->
            KeyboardOptions(keyboardType = KeyboardType.Text)
        else -> KeyboardOptions.Default
    }

    fun visualTransformation(type: PrimerInputElementType): VisualTransformation = when (type) {
        PrimerInputElementType.EXPIRY_DATE -> ExpiryDateVisualTransformation()
        else -> VisualTransformation.None
    }

    fun maxLength(type: PrimerInputElementType): Int? = when (type) {
        PrimerInputElementType.EXPIRY_DATE -> 6
        else -> null
    }

    fun allowedChars(type: PrimerInputElementType): String? = when (type) {
        PrimerInputElementType.EXPIRY_DATE, PrimerInputElementType.OTP_CODE -> "0123456789"
        else -> null
    }
}

@Composable
private fun CardFormScope.Input(
    modifier: Modifier = Modifier,
    type: PrimerInputElementType,
    onValueChange: (String) -> Unit,
) {
    val state by state.collectAsState()

    // Check if this field should be shown
    val isFieldRequired = type in state.cardFields || type in state.billingFields
    if (!isFieldRequired) return

    val value = state.inputFields[type] ?: ""
    val error = state.fieldErrors.find { it.inputElementType == type }

    // Apply essential input filtering while letting validation framework provide feedback
    val processedOnValueChange: (String) -> Unit = { newValue ->
        var processedValue = newValue

        // Apply allowed characters filter for strict input types (like CVV, card numbers)
        InputConfigs.allowedChars(type)?.let { allowedChars ->
            processedValue = newValue.filter { it in allowedChars }
        }

        // Apply max length constraint to prevent excessive input
        InputConfigs.maxLength(type)?.let { maxLength ->
            if (processedValue.length > maxLength) {
                processedValue = processedValue.take(maxLength)
            }
        }

        onValueChange(processedValue)
    }

    OutlinedTextField(
        value = value,
        onValueChange = processedOnValueChange,
        label = { Text(InputConfigs.label(type)) },
        placeholder = { Text(InputConfigs.placeholder(type)) },
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
        visualTransformation = InputConfigs.visualTransformation(type),
        keyboardOptions = InputConfigs.keyboardOptions(type),
    )
}

@Composable
internal fun CardFormScope.CardNumberInput(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()

    // Check if this field should be shown
    val isFieldRequired = PrimerInputElementType.CARD_NUMBER in state.cardFields || PrimerInputElementType.CARD_NUMBER in state.billingFields
    if (!isFieldRequired) return

    val value = state.inputFields[PrimerInputElementType.CARD_NUMBER] ?: ""
    val error = state.fieldErrors.find { it.inputElementType == PrimerInputElementType.CARD_NUMBER }
    val formatter = CardNumberFormatter.fromString(value)

    // Apply essential input filtering for card numbers
    val processedOnValueChange: (String) -> Unit = { newValue ->
        var processedValue = newValue.filter { it in "0123456789" }

        val maxLength = formatter.getMaxLength()
        if (processedValue.length > maxLength) {
            processedValue = processedValue.take(maxLength)
        }

        updateCardNumber(processedValue)
    }

    OutlinedTextField(
        value = value,
        onValueChange = processedOnValueChange,
        label = { Text("Card Number") },
        placeholder = { Text("1234 5678 9012 3456") },
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
        visualTransformation = CardNumberVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
internal fun CardFormScope.CvvInput(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()

    // Check if this field should be shown
    val isFieldRequired = PrimerInputElementType.CVV in state.cardFields || PrimerInputElementType.CVV in state.billingFields
    if (!isFieldRequired) return

    val value = state.inputFields[PrimerInputElementType.CVV] ?: ""
    val error = state.fieldErrors.find { it.inputElementType == PrimerInputElementType.CVV }
    val cardNumber = state.inputFields[PrimerInputElementType.CARD_NUMBER] ?: ""
    val formatter = CardNumberFormatter.fromString(cardNumber)
    val cvvLength = formatter.getCvvLength()

    // Apply essential input filtering for CVV
    val processedOnValueChange: (String) -> Unit = { newValue ->
        var processedValue = newValue.filter { it in "0123456789" }

        if (processedValue.length > cvvLength) {
            processedValue = processedValue.take(cvvLength)
        }

        updateCvv(processedValue)
    }

    OutlinedTextField(
        value = value,
        onValueChange = processedOnValueChange,
        label = { Text("CVV") },
        placeholder = { Text("1".repeat(cvvLength)) },
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
internal fun CardFormScope.ExpiryDateInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.EXPIRY_DATE, ::updateExpiryDate)

@Composable
internal fun CardFormScope.CardholderNameInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.CARDHOLDER_NAME, ::updateCardholderName)

@Composable
internal fun CardFormScope.PostalCodeInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.POSTAL_CODE, ::updatePostalCode)

@Composable
internal fun CardFormScope.CountryCodeInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.COUNTRY_CODE, ::updateCountryCode)

@Composable
internal fun CardFormScope.CityInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.CITY, ::updateCity)

@Composable
internal fun CardFormScope.StateInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.STATE, ::updateState)

@Composable
internal fun CardFormScope.AddressLine1Input(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.ADDRESS_LINE_1, ::updateAddressLine1)

@Composable
internal fun CardFormScope.AddressLine2Input(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.ADDRESS_LINE_2, ::updateAddressLine2)

@Composable
internal fun CardFormScope.PhoneNumberInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.PHONE_NUMBER, ::updatePhoneNumber)

@Composable
internal fun CardFormScope.FirstNameInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.FIRST_NAME, ::updateFirstName)

@Composable
internal fun CardFormScope.LastNameInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.LAST_NAME, ::updateLastName)

@Composable
internal fun CardFormScope.RetailOutletInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.RETAIL_OUTLET, ::updateRetailOutlet)

@Composable
internal fun CardFormScope.OtpCodeInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.OTP_CODE, ::updateOtpCode)
