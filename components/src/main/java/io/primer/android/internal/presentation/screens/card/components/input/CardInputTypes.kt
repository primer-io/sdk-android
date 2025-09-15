@file:Suppress("TooManyFunctions")

package io.primer.android.internal.presentation.screens.card.components.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.internal.presentation.components.PrimerInput
import io.primer.android.internal.presentation.screens.card.components.input.transformations.CardNumberVisualTransformation
import io.primer.android.scope.PrimerCardFormScope
import io.primer.cardShared.CardNumberFormatter

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
internal fun PrimerCardFormScope.CardNumberInput(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsStateWithLifecycle()

    val value = state.data[PrimerInputElementType.CARD_NUMBER].orEmpty()
    val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.CARD_NUMBER }
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
        trailingIcon = { with(components) { cardNetwork(modifier) } },
        visualTransformation = CardNumberVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        forceLtrForNumbers = true,
    )
}

@Composable
internal fun PrimerCardFormScope.CvvInput(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsStateWithLifecycle()

    // Check if this field should be shown
    val isFieldRequired =
        PrimerInputElementType.CVV in state.cardFields || PrimerInputElementType.CVV in state.billingFields
    if (!isFieldRequired) return

    val value = state.data[PrimerInputElementType.CVV] ?: ""
    val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.CVV }
    val cardNumber = state.data[PrimerInputElementType.CARD_NUMBER] ?: ""
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
                contentDescription = null,
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        forceLtrForNumbers = true,
    )
}

@Composable
internal fun PrimerCardFormScope.CountryCodeInput(modifier: Modifier = Modifier) {
    val state by state.collectAsStateWithLifecycle()

    // Check if this field should be shown
    val isFieldRequired = PrimerInputElementType.COUNTRY_CODE in state.cardFields ||
        PrimerInputElementType.COUNTRY_CODE in state.billingFields
    if (!isFieldRequired) return

    val selectedCountry = state.selectedCountry
    val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.COUNTRY_CODE }

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
                indication = null,
            ) { navigateToCountrySelection() },
        error = resolveErrorMessage(error),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedFocus,
            unfocusedBorderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedDefault,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
