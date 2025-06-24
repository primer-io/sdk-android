package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.primer.android.components.assets.ui.getCardImageAsset
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.displayMetadata.domain.model.ImageColor
import io.primer.android.ui.core.model.SyncValidationError
import io.primer.cardShared.CardNumberFormatter
import io.primer.composable.R
import io.primer.composable.internal.presentation.components.PrimerInput
import io.primer.composable.internal.presentation.screens.card.components.input.transformations.CardNumberVisualTransformation
import io.primer.composable.internal.presentation.screens.card.components.input.transformations.ExpiryDateVisualTransformation
import io.primer.composable.scope.PrimerCardFormScope

@Composable
private fun getInputLabel(type: PrimerInputElementType): String {
    val context = LocalContext.current
    return when (type) {
        PrimerInputElementType.CARDHOLDER_NAME -> context.getString(R.string.primer_components_card_form_name_on_card)
        PrimerInputElementType.EXPIRY_DATE -> context.getString(R.string.primer_components_card_form_expiry_date)
        PrimerInputElementType.POSTAL_CODE -> context.getString(R.string.primer_components_card_form_postal_code)
        PrimerInputElementType.COUNTRY_CODE -> context.getString(R.string.primer_components_card_form_country_code)
        PrimerInputElementType.CITY -> context.getString(R.string.primer_components_card_form_city)
        PrimerInputElementType.STATE -> context.getString(R.string.primer_components_card_form_state)
        PrimerInputElementType.ADDRESS_LINE_1 -> context.getString(R.string.primer_components_card_form_address_line_1)
        PrimerInputElementType.ADDRESS_LINE_2 -> context.getString(R.string.primer_components_card_form_address_line_2)
        PrimerInputElementType.PHONE_NUMBER -> context.getString(R.string.primer_components_card_form_phone_number)
        PrimerInputElementType.FIRST_NAME -> context.getString(R.string.primer_components_card_form_first_name)
        PrimerInputElementType.LAST_NAME -> context.getString(R.string.primer_components_card_form_last_name)
        PrimerInputElementType.RETAIL_OUTLET -> context.getString(R.string.primer_components_card_form_retail_outlet)
        PrimerInputElementType.OTP_CODE -> context.getString(R.string.primer_components_card_form_otp_code)
        else -> type.field
    }
}

@Composable
private fun getInputPlaceholder(type: PrimerInputElementType): String {
    val context = LocalContext.current
    return when (type) {
        PrimerInputElementType.CARDHOLDER_NAME -> context.getString(R.string.primer_components_card_form_placeholder_full_name)
        PrimerInputElementType.EXPIRY_DATE -> context.getString(R.string.primer_components_card_form_placeholder_expiry_date)
        PrimerInputElementType.POSTAL_CODE -> context.getString(R.string.primer_components_card_form_placeholder_postal_code)
        PrimerInputElementType.COUNTRY_CODE -> context.getString(R.string.primer_components_card_form_placeholder_country_code)
        PrimerInputElementType.CITY -> context.getString(R.string.primer_components_card_form_placeholder_city)
        PrimerInputElementType.STATE -> context.getString(R.string.primer_components_card_form_placeholder_state)
        PrimerInputElementType.ADDRESS_LINE_1 -> context.getString(R.string.primer_components_card_form_placeholder_address_line_1)
        PrimerInputElementType.ADDRESS_LINE_2 -> context.getString(R.string.primer_components_card_form_placeholder_address_line_2)
        PrimerInputElementType.PHONE_NUMBER -> context.getString(R.string.primer_components_card_form_placeholder_phone_number)
        PrimerInputElementType.FIRST_NAME -> context.getString(R.string.primer_components_card_form_placeholder_first_name)
        PrimerInputElementType.LAST_NAME -> context.getString(R.string.primer_components_card_form_placeholder_last_name)
        PrimerInputElementType.RETAIL_OUTLET -> context.getString(R.string.primer_components_card_form_placeholder_retail_outlet)
        PrimerInputElementType.OTP_CODE -> context.getString(R.string.primer_components_card_form_placeholder_otp_code)
        else -> ""
    }
}

private object InputConfigs {


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
        PrimerInputElementType.EXPIRY_DATE -> 4
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
private fun resolveErrorMessage(error: SyncValidationError?): String? {
    if (error == null) return null
    
    val context = LocalContext.current
    
    return error.errorFormatId?.let { formatId ->
        // Try to get the field name string resource
        val fieldName = try {
            context.getString(error.fieldId)
        } catch (e: Exception) {
            context.getString(R.string.primer_components_card_form_field) // Fallback if fieldId resource doesn't exist
        }
        context.getString(formatId, fieldName)
    } ?: error.errorResId?.let { resId ->
        context.getString(resId)
    }
    // If neither errorFormatId nor errorResId are available, fall back to errorId
    ?: error.errorId
}

@Composable
internal fun CardNetworkIcon(
    cardNetwork: CardNetwork.Type,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Icon(
        painter = painterResource(id = cardNetwork.getCardImageAsset(ImageColor.COLORED)),
        contentDescription = context.getString(R.string.primer_components_content_description_card_network, cardNetwork.name),
        modifier = modifier.size(20.dp),
        tint = androidx.compose.ui.graphics.Color.Unspecified
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrimerCardFormScope.CardInput(
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

    PrimerInput(
        value = value,
        onValueChange = processedOnValueChange,
        label = getInputLabel(type),
        placeholder = getInputPlaceholder(type),
        modifier = modifier.fillMaxWidth(),
        error = resolveErrorMessage(error),
        trailingIcon = {
            InputConfigs.trailingIcon(type)?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null
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

    PrimerInput(
        value = value,
        onValueChange = processedOnValueChange,
        label = stringResource(R.string.primer_components_card_form_card_number),
        placeholder = stringResource(R.string.primer_components_card_form_placeholder_card_number),
        modifier = modifier.fillMaxWidth(),
        error = resolveErrorMessage(error),
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

    PrimerInput(
        value = value,
        onValueChange = processedOnValueChange,
        label = stringResource(R.string.primer_components_card_form_cvv),
        placeholder = "1".repeat(cvvLength),
        modifier = modifier.fillMaxWidth(),
        error = resolveErrorMessage(error),
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_primer_card_cvv),
                contentDescription = null
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
internal fun PrimerCardFormScope.ExpiryDateInput(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.EXPIRY_DATE, ::updateExpiryDate)

@Composable
internal fun PrimerCardFormScope.CardholderNameInput(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.CARDHOLDER_NAME, ::updateCardholderName)

@Composable
internal fun PrimerCardFormScope.PostalCodeInput(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.POSTAL_CODE, ::updatePostalCode)

@Composable
internal fun PrimerCardFormScope.CountryCodeInput(modifier: Modifier = Modifier) {
    val state by state.collectAsState()

    // Check if this field should be shown
    val isFieldRequired = PrimerInputElementType.COUNTRY_CODE in state.cardFields ||
        PrimerInputElementType.COUNTRY_CODE in state.billingFields
    if (!isFieldRequired) return

    val selectedCountry = state.selectedCountry
    val error = state.fieldErrors.find { it.inputElementType == PrimerInputElementType.COUNTRY_CODE }

    PrimerInput(
        value = selectedCountry?.name ?: "",
        onValueChange = { },
        readOnly = true,
        enabled = false,
        label = stringResource(R.string.primer_components_card_form_country),
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { navigateToCountrySelection() },
        error = resolveErrorMessage(error),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}

@Composable
internal fun PrimerCardFormScope.CityInput(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.CITY, ::updateCity)

@Composable
internal fun PrimerCardFormScope.StateInput(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.STATE, ::updateState)

@Composable
internal fun PrimerCardFormScope.AddressLine1Input(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.ADDRESS_LINE_1, ::updateAddressLine1)

@Composable
internal fun PrimerCardFormScope.AddressLine2Input(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.ADDRESS_LINE_2, ::updateAddressLine2)

@Composable
internal fun PrimerCardFormScope.PhoneNumberInput(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.PHONE_NUMBER, ::updatePhoneNumber)

@Composable
internal fun PrimerCardFormScope.FirstNameInput(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.FIRST_NAME, ::updateFirstName)

@Composable
internal fun PrimerCardFormScope.LastNameInput(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.LAST_NAME, ::updateLastName)

@Composable
internal fun PrimerCardFormScope.RetailOutletInput(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.RETAIL_OUTLET, ::updateRetailOutlet)

@Composable
internal fun PrimerCardFormScope.OtpCodeInput(modifier: Modifier = Modifier) =
    CardInput(modifier, PrimerInputElementType.OTP_CODE, ::updateOtpCode)
