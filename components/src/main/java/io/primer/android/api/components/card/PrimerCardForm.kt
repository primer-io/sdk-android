package io.primer.android.api.components.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.emojiFlag
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.internal.presentation.components.DefaultSubmitButton
import io.primer.android.internal.presentation.components.PrimerInput
import io.primer.android.internal.presentation.screens.card.components.DefaultCardNetworkIcon
import io.primer.android.internal.presentation.screens.card.components.input.CardInput
import io.primer.android.internal.presentation.screens.card.components.input.resolveErrorMessage
import io.primer.android.internal.presentation.screens.card.components.input.transformations.CardNumberVisualTransformation
import io.primer.android.internal.presentation.utils.toWesternNumeralsOnly
import io.primer.cardShared.CardNumberFormatter

/**
 * Card payment form with validation and submission.
 *
 * Displays a complete card form with:
 * - Card number (with network detection and co-badge selection)
 * - Expiry date and CVV
 * - Cardholder name (if required)
 * - Billing address (if required by configuration)
 * - Submit button
 *
 * Fields are configured automatically based on your Primer dashboard settings.
 * Validation is handled by the SDK with real-time feedback.
 *
 * ## Basic usage
 * ```kotlin
 * val controller = rememberCardFormController(checkout)
 * PrimerCardForm(controller = controller)
 * ```
 *
 * ## Custom submit button
 * ```kotlin
 * val controller = rememberCardFormController(checkout)
 * val formState by controller.state.collectAsStateWithLifecycle()
 *
 * PrimerCardForm(
 *     controller = controller,
 *     submitButton = {
 *         MyBrandButton(
 *             text = "Pay ${checkout.formatAmount(formState.amount)}",
 *             enabled = formState.isFormValid && !formState.isLoading,
 *             onClick = { controller.submit() }
 *         )
 *     }
 * )
 * ```
 *
 * ## Custom field layout
 * ```kotlin
 * PrimerCardForm(
 *     controller = controller,
 *     cardDetails = {
 *         // Rearrange or style individual fields
 *         Column {
 *             CardFormDefaults.CardNumberField()
 *             Row {
 *                 CardFormDefaults.ExpiryField(Modifier.weight(1f))
 *                 Spacer(Modifier.width(8.dp))
 *                 CardFormDefaults.CvvField(Modifier.weight(1f))
 *             }
 *             CardFormDefaults.CardholderField()
 *         }
 *     }
 * )
 * ```
 *
 * @param controller Card form controller from [rememberCardFormController]
 * @param modifier Modifier for the form container
 * @param cardDetails Content for card details section (number, expiry, CVV, cardholder)
 * @param billingAddress Content for billing address section (shown if required)
 * @param submitButton Submit button content
 */
@Composable
fun PrimerCardForm(
    controller: PrimerCardFormController,
    modifier: Modifier = Modifier,
    cardDetails: @Composable () -> Unit = { CardFormDefaults.CardDetailsContent(controller) },
    billingAddress: @Composable () -> Unit = { CardFormDefaults.BillingAddressContent(controller) },
    submitButton: @Composable () -> Unit = { CardFormDefaults.SubmitButton(controller) },
) {
    val theme = LocalPrimerTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        cardDetails()
        billingAddress()
        Spacer(modifier = Modifier.height(theme.spacingTokens.xsmall))
        submitButton()
    }
}

/**
 * Default field implementations for [PrimerCardForm].
 *
 * Use these to customize the card form layout while keeping individual
 * field behavior (validation, formatting, keyboard type).
 *
 * ## Rearranging fields
 * ```kotlin
 * val controller = rememberCardFormController(checkout)
 *
 * PrimerCardForm(
 *     controller = controller,
 *     cardDetails = {
 *         Column {
 *             CardFormDefaults.CardholderField(controller) // Name first
 *             CardFormDefaults.CardNumberField(controller)
 *             Row {
 *                 CardFormDefaults.ExpiryField(controller, Modifier.weight(1f))
 *                 CardFormDefaults.CvvField(controller, Modifier.weight(1f))
 *             }
 *         }
 *     }
 * )
 * ```
 */
@Suppress("TooManyFunctions")
@OptIn(ExperimentalPrimerApi::class)
object CardFormDefaults {

    // ============================================================
    // Card Field Components
    // ============================================================

    /**
     * Card number input with automatic formatting (groups of 4 digits)
     * and network detection icon.
     */
    @Composable
    fun CardNumberField(cardFormController: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormController.state.collectAsStateWithLifecycle()

        val value = state.data[PrimerInputElementType.CARD_NUMBER].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.CARD_NUMBER }
        val formatter = CardNumberFormatter.fromString(value)

        val processedOnValueChange: (String) -> Unit = { newValue ->
            var processedValue = newValue.toWesternNumeralsOnly()
            val maxLength = formatter.getMaxLength()
            if (processedValue.length > maxLength) {
                processedValue = processedValue.take(maxLength)
            }
            cardFormController.updateCardNumber(processedValue)
        }

        PrimerInput(
            value = value,
            onValueChange = processedOnValueChange,
            label = stringResource(R.string.primer_card_form_label_number),
            placeholder = stringResource(R.string.primer_card_form_placeholder_number),
            modifier = modifier
                .fillMaxWidth()
                .testTag("primer_input_card_number"),
            error = resolveErrorMessage(error),
            enabled = state.isFormEnabled,
            trailingIcon = { CardNetworkField(cardFormController) },
            visualTransformation = CardNumberVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            forceLtrForNumbers = false,
            onFocusChange = { hasFocus ->
                cardFormController.onFieldFocusChange(PrimerInputElementType.CARD_NUMBER, hasFocus)
            },
            accessibilityLabel = stringResource(R.string.accessibility_card_form_card_number_label),
        )
    }

    /**
     * Expiry date field (MM/YY format).
     */
    @Composable
    fun ExpiryField(cardFormController: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormController.state.collectAsStateWithLifecycle()

        val isFieldRequired = PrimerInputElementType.EXPIRY_DATE in state.cardFields
        if (!isFieldRequired) return

        val value = state.data[PrimerInputElementType.EXPIRY_DATE].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.EXPIRY_DATE }

        CardInput(
            type = PrimerInputElementType.EXPIRY_DATE,
            value = value,
            onValueChange = { cardFormController.updateExpiryDate(it) },
            modifier = modifier.fillMaxWidth(),
            error = error,
            enabled = state.isFormEnabled,
            onFocusChange = { hasFocus ->
                cardFormController.onFieldFocusChange(PrimerInputElementType.EXPIRY_DATE, hasFocus)
            },
        )
    }

    /**
     * CVV field with dynamic length based on card type.
     */
    @Composable
    fun CvvField(cardFormState: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormState.state.collectAsStateWithLifecycle()

        val isFieldRequired = PrimerInputElementType.CVV in state.cardFields ||
            PrimerInputElementType.CVV in state.billingFields
        if (!isFieldRequired) return

        val value = state.data[PrimerInputElementType.CVV].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.CVV }
        val cardNumber = state.data[PrimerInputElementType.CARD_NUMBER].orEmpty()
        val formatter = CardNumberFormatter.fromString(cardNumber)
        val cvvLength = formatter.getCvvLength()

        val processedOnValueChange: (String) -> Unit = { newValue ->
            var processedValue = newValue.toWesternNumeralsOnly()
            if (processedValue.length > cvvLength) {
                processedValue = processedValue.take(cvvLength)
            }
            cardFormState.updateCvv(processedValue)
        }

        PrimerInput(
            value = value,
            onValueChange = processedOnValueChange,
            label = stringResource(R.string.primer_card_form_label_cvv),
            placeholder = "1".repeat(cvvLength),
            modifier = modifier
                .fillMaxWidth()
                .testTag("primer_input_cvv"),
            error = resolveErrorMessage(error),
            enabled = state.isFormEnabled,
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_primer_card_cvv),
                    contentDescription = null,
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            forceLtrForNumbers = false,
            onFocusChange = { hasFocus ->
                cardFormState.onFieldFocusChange(PrimerInputElementType.CVV, hasFocus)
            },
            accessibilityLabel = stringResource(R.string.accessibility_card_form_cvc_label),
            isRequired = true,
        )
    }

    /**
     * Cardholder name field.
     */
    @Composable
    fun CardholderField(cardFormController: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormController.state.collectAsStateWithLifecycle()

        val isFieldRequired = PrimerInputElementType.CARDHOLDER_NAME in state.cardFields
        if (!isFieldRequired) return

        val value = state.data[PrimerInputElementType.CARDHOLDER_NAME].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.CARDHOLDER_NAME }

        CardInput(
            type = PrimerInputElementType.CARDHOLDER_NAME,
            value = value,
            onValueChange = { cardFormController.updateCardholderName(it) },
            modifier = modifier.fillMaxWidth(),
            error = error,
            enabled = state.isFormEnabled,
            onFocusChange = { hasFocus ->
                cardFormController.onFieldFocusChange(PrimerInputElementType.CARDHOLDER_NAME, hasFocus)
            },
        )
    }

    /**
     * Card network selector for co-badged cards.
     */
    @Composable
    fun CardNetworkField(cardFormState: PrimerCardFormController) {
        val state by cardFormState.state.collectAsStateWithLifecycle()

        DefaultCardNetworkIcon(networkSelection = state.networkSelection) {
            cardFormState.selectCardNetwork(network = it)
        }
    }

    // ============================================================
    // Billing Address Field Components
    // ============================================================

    /**
     * Country code field that opens a country selector when clicked.
     */
    @Composable
    fun CountryCodeField(cardFormState: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormState.state.collectAsStateWithLifecycle()
        val colorTokens = LocalPrimerTheme.current.colorTokens()
        val sizeTokens = LocalPrimerTheme.current.sizeTokens

        val isFieldRequired = PrimerInputElementType.COUNTRY_CODE in state.cardFields ||
            PrimerInputElementType.COUNTRY_CODE in state.billingFields
        if (!isFieldRequired) return

        val selectedCountry = state.selectedCountry
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.COUNTRY_CODE }

        val displayValue = selectedCountry?.let { country ->
            val flag = country.code.emojiFlag()
            "$flag ${country.name}"
        }.orEmpty()

        PrimerInput(
            value = displayValue,
            onValueChange = { },
            readOnly = true,
            enabled = false,
            label = stringResource(R.string.primer_card_form_label_country),
            placeholder = stringResource(R.string.primer_card_form_placeholder_country_code),
            modifier = modifier
                .fillMaxWidth()
                .testTag("primer_input_country_code")
                .semantics { role = Role.DropdownList }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { cardFormState.requestCountrySelection() },
            error = resolveErrorMessage(error),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_primer_chevron_right),
                    contentDescription = null,
                    tint = colorTokens.primerColorTextSecondary,
                    modifier = Modifier.size(sizeTokens.medium),
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorTokens.primerColorBorderOutlinedFocus,
                unfocusedBorderColor = colorTokens.primerColorBorderOutlinedDefault,
                disabledTextColor = colorTokens.primerColorTextPrimary,
                disabledBorderColor = colorTokens.primerColorBorderOutlinedDefault,
                disabledLabelColor = colorTokens.primerColorTextSecondary,
            ),
            onFocusChange = { hasFocus ->
                cardFormState.onFieldFocusChange(PrimerInputElementType.COUNTRY_CODE, hasFocus)
            },
            accessibilityLabel = stringResource(R.string.accessibility_card_form_billing_address_country_label),
        )
    }

    @Composable
    fun FirstNameField(cardFormState: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormState.state.collectAsStateWithLifecycle()

        val isFieldRequired = PrimerInputElementType.FIRST_NAME in state.billingFields
        if (!isFieldRequired) return

        val value = state.data[PrimerInputElementType.FIRST_NAME].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.FIRST_NAME }

        CardInput(
            type = PrimerInputElementType.FIRST_NAME,
            value = value,
            onValueChange = { cardFormState.updateFirstName(it) },
            modifier = modifier.fillMaxWidth(),
            error = error,
            enabled = state.isFormEnabled,
            onFocusChange = { hasFocus ->
                cardFormState.onFieldFocusChange(PrimerInputElementType.FIRST_NAME, hasFocus)
            },
        )
    }

    @Composable
    fun LastNameField(cardFormState: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormState.state.collectAsStateWithLifecycle()

        val isFieldRequired = PrimerInputElementType.LAST_NAME in state.billingFields
        if (!isFieldRequired) return

        val value = state.data[PrimerInputElementType.LAST_NAME].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.LAST_NAME }

        CardInput(
            type = PrimerInputElementType.LAST_NAME,
            value = value,
            onValueChange = { cardFormState.updateLastName(it) },
            modifier = modifier.fillMaxWidth(),
            error = error,
            enabled = state.isFormEnabled,
            onFocusChange = { hasFocus ->
                cardFormState.onFieldFocusChange(PrimerInputElementType.LAST_NAME, hasFocus)
            },
        )
    }

    @Composable
    fun AddressLine1Field(cardFormState: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormState.state.collectAsStateWithLifecycle()

        val isFieldRequired = PrimerInputElementType.ADDRESS_LINE_1 in state.billingFields
        if (!isFieldRequired) return

        val value = state.data[PrimerInputElementType.ADDRESS_LINE_1].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.ADDRESS_LINE_1 }

        CardInput(
            type = PrimerInputElementType.ADDRESS_LINE_1,
            value = value,
            onValueChange = { cardFormState.updateAddressLine1(it) },
            modifier = modifier.fillMaxWidth(),
            error = error,
            enabled = state.isFormEnabled,
            onFocusChange = { hasFocus ->
                cardFormState.onFieldFocusChange(PrimerInputElementType.ADDRESS_LINE_1, hasFocus)
            },
        )
    }

    @Composable
    fun AddressLine2Field(cardFormState: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormState.state.collectAsStateWithLifecycle()

        val isFieldRequired = PrimerInputElementType.ADDRESS_LINE_2 in state.billingFields
        if (!isFieldRequired) return

        val value = state.data[PrimerInputElementType.ADDRESS_LINE_2].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.ADDRESS_LINE_2 }

        CardInput(
            type = PrimerInputElementType.ADDRESS_LINE_2,
            value = value,
            onValueChange = { cardFormState.updateAddressLine2(it) },
            modifier = modifier.fillMaxWidth(),
            error = error,
            enabled = state.isFormEnabled,
            onFocusChange = { hasFocus ->
                cardFormState.onFieldFocusChange(PrimerInputElementType.ADDRESS_LINE_2, hasFocus)
            },
        )
    }

    @Composable
    fun CityField(cardFormState: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormState.state.collectAsStateWithLifecycle()

        val isFieldRequired = PrimerInputElementType.CITY in state.billingFields
        if (!isFieldRequired) return

        val value = state.data[PrimerInputElementType.CITY].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.CITY }

        CardInput(
            type = PrimerInputElementType.CITY,
            value = value,
            onValueChange = { cardFormState.updateCity(it) },
            modifier = modifier.fillMaxWidth(),
            error = error,
            enabled = state.isFormEnabled,
            onFocusChange = { hasFocus ->
                cardFormState.onFieldFocusChange(PrimerInputElementType.CITY, hasFocus)
            },
        )
    }

    @Composable
    fun StateField(cardFormState: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormState.state.collectAsStateWithLifecycle()

        val isFieldRequired = PrimerInputElementType.STATE in state.billingFields
        if (!isFieldRequired) return

        val value = state.data[PrimerInputElementType.STATE].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.STATE }

        CardInput(
            type = PrimerInputElementType.STATE,
            value = value,
            onValueChange = { cardFormState.updateState(it) },
            modifier = modifier.fillMaxWidth(),
            error = error,
            enabled = state.isFormEnabled,
            onFocusChange = { hasFocus ->
                cardFormState.onFieldFocusChange(PrimerInputElementType.STATE, hasFocus)
            },
        )
    }

    @Composable
    fun PostalCodeField(cardFormState: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormState.state.collectAsStateWithLifecycle()

        val isFieldRequired = PrimerInputElementType.POSTAL_CODE in state.billingFields
        if (!isFieldRequired) return

        val value = state.data[PrimerInputElementType.POSTAL_CODE].orEmpty()
        val error = state.fieldErrors?.find { it.inputElementType == PrimerInputElementType.POSTAL_CODE }

        CardInput(
            type = PrimerInputElementType.POSTAL_CODE,
            value = value,
            onValueChange = { cardFormState.updatePostalCode(it) },
            modifier = modifier.fillMaxWidth(),
            error = error,
            enabled = state.isFormEnabled,
            onFocusChange = { hasFocus ->
                cardFormState.onFieldFocusChange(PrimerInputElementType.POSTAL_CODE, hasFocus)
            },
        )
    }

    // ============================================================
    // Layout Components
    // ============================================================

    /**
     * Default card details layout (card number with network selector, expiry, CVV, cardholder name).
     */
    @Composable
    fun CardDetailsContent(
        cardFormState: PrimerCardFormController,
        cardNumber: @Composable () -> Unit = { CardNumberField(cardFormState) },
        expiryDate: @Composable () -> Unit = { ExpiryField(cardFormState) },
        cvv: @Composable () -> Unit = { CvvField(cardFormState) },
        cardholderName: @Composable () -> Unit = { CardholderField(cardFormState) },
    ) {
        val spacingSmall = LocalPrimerTheme.current.spacingTokens.small

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    cardNumber()
                }
            }
            Spacer(modifier = Modifier.height(spacingSmall))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    expiryDate()
                }
                Spacer(modifier = Modifier.width(spacingSmall))
                Box(modifier = Modifier.weight(1f)) {
                    cvv()
                }
            }
            Spacer(modifier = Modifier.height(spacingSmall))
            cardholderName()
            Spacer(modifier = Modifier.height(spacingSmall))
        }
    }

    /**
     * Default billing address layout.
     * Automatically shows/hides based on billing fields in state.
     * Country field click navigates to country selection internally.
     */
    @Composable
    fun BillingAddressContent(
        cardFormState: PrimerCardFormController,
        countryCode: @Composable () -> Unit = { CountryCodeField(cardFormState) },
        firstName: @Composable () -> Unit = { FirstNameField(cardFormState) },
        lastName: @Composable () -> Unit = { LastNameField(cardFormState) },
        addressLine1: @Composable () -> Unit = { AddressLine1Field(cardFormState) },
        addressLine2: @Composable () -> Unit = { AddressLine2Field(cardFormState) },
        city: @Composable () -> Unit = { CityField(cardFormState) },
        stateField: @Composable () -> Unit = { StateField(cardFormState) },
        postalCode: @Composable () -> Unit = { PostalCodeField(cardFormState) },
    ) {
        val state by cardFormState.state.collectAsStateWithLifecycle()
        val billingFields = state.billingFields
        if (billingFields.isEmpty()) return

        val spacingSmall = LocalPrimerTheme.current.spacingTokens.small
        val billingSectionDescription = stringResource(R.string.accessibility_card_form_billing_section)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = billingSectionDescription
                    heading()
                },
        ) {
            countryCode()
            Spacer(modifier = Modifier.height(spacingSmall))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    firstName()
                }
                Spacer(modifier = Modifier.width(spacingSmall))
                Box(modifier = Modifier.weight(1f)) {
                    lastName()
                }
            }
            Spacer(modifier = Modifier.height(spacingSmall))
            addressLine1()
            Spacer(modifier = Modifier.height(spacingSmall))
            addressLine2()
            Spacer(modifier = Modifier.height(spacingSmall))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    postalCode()
                }
                Spacer(modifier = Modifier.width(spacingSmall))
                Box(modifier = Modifier.weight(1f)) {
                    city()
                }
            }
            Spacer(modifier = Modifier.height(spacingSmall))
            stateField()
        }
    }

    /**
     * Default submit button using SDK's styled button.
     */
    @Composable
    fun SubmitButton(cardFormState: PrimerCardFormController, modifier: Modifier = Modifier) {
        val state by cardFormState.state.collectAsStateWithLifecycle()

        DefaultSubmitButton(
            modifier = modifier,
            isLoading = state.isLoading,
            enabled = !state.isLoading && state.isFormValid,
            onClick = { cardFormState.submit() },
        )
    }
}
