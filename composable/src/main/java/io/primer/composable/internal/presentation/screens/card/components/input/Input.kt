package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.primer.android.components.assets.ui.getCardImageAsset
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.displayMetadata.domain.model.ImageColor
import io.primer.cardShared.CardNumberFormatter
import io.primer.composable.R
import io.primer.composable.internal.presentation.screens.card.components.input.transformations.CardNumberVisualTransformation
import io.primer.composable.internal.presentation.screens.card.components.input.transformations.ExpiryDateVisualTransformation
import io.primer.composable.scope.PrimerCardFormScope

private object InputConfigs {
    fun label(type: PrimerInputElementType): String = when (type) {
        PrimerInputElementType.CARDHOLDER_NAME -> "Name on card"
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
        PrimerInputElementType.CARDHOLDER_NAME -> "Full name"
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

    fun trailingIcon(type: PrimerInputElementType): Int? = when (type) {
        PrimerInputElementType.EXPIRY_DATE -> R.drawable.ic_primer_card_expiry_date
        else -> null
    }
}

@Composable
internal fun CardNetworkIcon(
    cardNetwork: CardNetwork.Type,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(id = cardNetwork.getCardImageAsset(ImageColor.COLORED)),
        contentDescription = "Card network: ${cardNetwork.name}",
        modifier = modifier.size(20.dp),
        tint = androidx.compose.ui.graphics.Color.Unspecified
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrimerCardFormScope.Input(
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
        trailingIcon = {
            InputConfigs.trailingIcon(type)?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = "Trailing"
                )
            }
        },
        visualTransformation = InputConfigs.visualTransformation(type),
        keyboardOptions = InputConfigs.keyboardOptions(type),
    )
}

@Composable
internal fun PrimerCardFormScope.CardNumberInput(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()

    // Check if this field should be shown
    val isFieldRequired = PrimerInputElementType.CARD_NUMBER in state.cardFields ||
        PrimerInputElementType.CARD_NUMBER in state.billingFields
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
        placeholder = { Text("1234 1234 1234 1234") },
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
        trailingIcon = { CardNetworkIcon(cardNetwork = state.detectedCardNetwork) },
        visualTransformation = CardNumberVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
internal fun PrimerCardFormScope.CvvInput(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsState()

    // Check if this field should be shown
    val isFieldRequired =
        PrimerInputElementType.CVV in state.cardFields || PrimerInputElementType.CVV in state.billingFields
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
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_primer_card_cvv),
                contentDescription = "Trailing"
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
internal fun PrimerCardFormScope.ExpiryDateInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.EXPIRY_DATE, ::updateExpiryDate)

@Composable
internal fun PrimerCardFormScope.CardholderNameInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.CARDHOLDER_NAME, ::updateCardholderName)

@Composable
internal fun PrimerCardFormScope.PostalCodeInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.POSTAL_CODE, ::updatePostalCode)

@Composable
internal fun PrimerCardFormScope.CountryCodeInput(modifier: Modifier = Modifier) {
    val state by state.collectAsState()

    // Check if this field should be shown
    val isFieldRequired = PrimerInputElementType.COUNTRY_CODE in state.cardFields ||
        PrimerInputElementType.COUNTRY_CODE in state.billingFields
    if (!isFieldRequired) return

    val selectedCountry = state.selectedCountry
    val error = state.fieldErrors.find { it.inputElementType == PrimerInputElementType.COUNTRY_CODE }

    OutlinedTextField(
        value = selectedCountry?.name ?: "",
        onValueChange = { },
        readOnly = true,
        enabled = false,
        label = { Text("Country") },
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { navigateToCountrySelection() },
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
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}

@Composable
internal fun PrimerCardFormScope.CityInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.CITY, ::updateCity)

@Composable
internal fun PrimerCardFormScope.StateInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.STATE, ::updateState)

@Composable
internal fun PrimerCardFormScope.AddressLine1Input(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.ADDRESS_LINE_1, ::updateAddressLine1)

@Composable
internal fun PrimerCardFormScope.AddressLine2Input(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.ADDRESS_LINE_2, ::updateAddressLine2)

@Composable
internal fun PrimerCardFormScope.PhoneNumberInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.PHONE_NUMBER, ::updatePhoneNumber)

@Composable
internal fun PrimerCardFormScope.FirstNameInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.FIRST_NAME, ::updateFirstName)

@Composable
internal fun PrimerCardFormScope.LastNameInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.LAST_NAME, ::updateLastName)

@Composable
internal fun PrimerCardFormScope.RetailOutletInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.RETAIL_OUTLET, ::updateRetailOutlet)

@Composable
internal fun PrimerCardFormScope.OtpCodeInput(modifier: Modifier = Modifier) =
    Input(modifier, PrimerInputElementType.OTP_CODE, ::updateOtpCode)
