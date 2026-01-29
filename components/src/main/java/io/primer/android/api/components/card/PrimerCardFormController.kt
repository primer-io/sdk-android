package io.primer.android.api.components.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.screens.card.CardFormViewModel
import io.primer.android.internal.presentation.screens.card.CardFormViewModelFactory
import io.primer.android.ui.core.model.SyncValidationError
import kotlinx.coroutines.flow.StateFlow

/**
 * Card form state and actions for building custom card form UI.
 *
 * Provides access to form state (field values, validation, loading) and
 * actions (update fields, submit). Create via [rememberCardFormController].
 *
 * ## State observation
 * ```kotlin
 * val cardFormState = rememberCardFormState(checkout)
 * val state by cardFormState.state.collectAsStateWithLifecycle()
 *
 * // Access field values
 * val cardNumber = state.data[PrimerInputElementType.CARD_NUMBER].orEmpty()
 * val isValid = state.isFormValid
 * val isLoading = state.isLoading
 * ```
 *
 * ## Field updates
 * ```kotlin
 * cardFormState.updateCardNumber("4242424242424242")
 * cardFormState.updateExpiryDate("12/25")
 * cardFormState.updateCvv("123")
 * ```
 *
 * ## Form submission
 * ```kotlin
 * if (state.isFormValid) {
 *     cardFormState.submit()
 * }
 * ```
 */
@Stable
interface PrimerCardFormController {
    /** Observable form state including field values, validation, and loading status. */
    val state: StateFlow<State>

    /** Update card number field. */
    fun updateCardNumber(cardNumber: String)

    /** Update CVV field. */
    fun updateCvv(cvv: String)

    /** Update expiry date field (format: MM/YY). */
    fun updateExpiryDate(expiryDate: String)

    /** Update cardholder name field. */
    fun updateCardholderName(cardholderName: String)

    /** Update postal code field. */
    fun updatePostalCode(postalCode: String)

    /** Update country code field. */
    fun updateCountryCode(countryCode: String)

    /** Update city field. */
    fun updateCity(city: String)

    /** Update state/region field. */
    fun updateState(state: String)

    /** Update address line 1 field. */
    fun updateAddressLine1(addressLine1: String)

    /** Update address line 2 field. */
    fun updateAddressLine2(addressLine2: String)

    /** Update phone number field. */
    fun updatePhoneNumber(phoneNumber: String)

    /** Update first name field. */
    fun updateFirstName(firstName: String)

    /** Update last name field. */
    fun updateLastName(lastName: String)

    /** Submit the form to process payment. */
    fun submit()

    /** Select card network for co-badged cards. */
    fun selectCardNetwork(network: PrimerCardNetwork)

    /** Called when a field gains or loses focus. Used for validation timing. */
    fun onFieldFocusChange(field: PrimerInputElementType, hasFocus: Boolean)

    /** Called when country field is clicked to open country selector. */
    fun requestCountrySelection()

    /** Enable or disable vaulting on successful payment. */
    fun setVaultOnSuccess(enabled: Boolean)

    /**
     * Form state containing field values, validation status, and UI state.
     *
     * @property cardFields Required card fields (card number, expiry, CVV, etc.)
     * @property billingFields Required billing address fields
     * @property fieldErrors Current validation errors for fields
     * @property data Current field values keyed by field type
     * @property isLoading True while form is submitting
     * @property isFormEnabled True if form inputs are enabled
     * @property selectedCountry Currently selected country for billing
     * @property networkSelection Network selection for co-badged cards
     * @property fieldFocusStates Focus state for each field (for validation display)
     * @property isFormValid True if all required fields are valid
     * @property vaultOnSuccess Whether to save card on successful payment
     */
    @Immutable
    data class State(
        val cardFields: List<PrimerInputElementType> = emptyList(),
        val billingFields: List<PrimerInputElementType> = emptyList(),
        val fieldErrors: List<SyncValidationError>? = emptyList(),
        val data: Map<PrimerInputElementType, String> = emptyMap(),
        val isLoading: Boolean = false,
        val isFormEnabled: Boolean = true,
        val selectedCountry: PrimerCountry? = null,
        val networkSelection: NetworkSelection = NetworkSelection(),
        val fieldFocusStates: Map<PrimerInputElementType, FieldState> = emptyMap(),
        val isFormValid: Boolean = false,
        val vaultOnSuccess: Boolean = false,
    )

    @Immutable
    data class NetworkSelection(
        val selectedNetwork: PrimerCardNetwork? = null,
        val availableNetworks: List<PrimerCardNetwork> = emptyList(),
        val isNetworkSelectable: Boolean = true,
    )

    /**
     * Focus state for a single field.
     *
     * @property hasFocus True if field currently has focus
     * @property hasBeenFocused True if field has ever received focus
     * @property shouldShowError True if error should be displayed (based on focus history)
     */
    @Immutable
    data class FieldState(
        val hasFocus: Boolean = false,
        val hasBeenFocused: Boolean = false,
        val shouldShowError: Boolean = false,
    )
}

/**
 * Creates and remembers card form state for building custom card form UI.
 *
 * This provides both state and actions, allowing you to build completely
 * custom card form UI without using the default [CardFormDefaults] components.
 *
 * The returned [PrimerCardFormController] contains:
 * - **State**: `state: StateFlow<State>` with card/billing fields, validation, loading state
 * - **Field updates**: `updateCardNumber()`, `updateCvv()`, `updateExpiryDate()`, etc.
 * - **Submit**: `submit()` to submit the form
 * - **Network selection**: `selectCardNetwork()` for co-badged cards
 *
 * ## Example: Fully custom card form
 * ```kotlin
 * val cardFormState = rememberCardFormState(checkout)
 * val state by cardFormState.state.collectAsStateWithLifecycle()
 *
 * Column {
 *     // Custom card number input
 *     MyTextField(
 *         value = state.data[PrimerInputElementType.CARD_NUMBER].orEmpty(),
 *         onValueChange = { cardFormState.updateCardNumber(it) },
 *     )
 *
 *     // Custom expiry input
 *     MyTextField(
 *         value = state.data[PrimerInputElementType.EXPIRY_DATE].orEmpty(),
 *         onValueChange = { cardFormState.updateExpiryDate(it) }
 *     )
 *
 *     // Custom CVV input
 *     MyTextField(
 *         value = state.data[PrimerInputElementType.CVV].orEmpty(),
 *         onValueChange = { cardFormState.updateCvv(it) }
 *     )
 *
 *     // Custom submit button
 *     MyButton(
 *         enabled = state.isFormValid && !state.isLoading,
 *         onClick = { cardFormState.submit() }
 *     ) {
 *         Text("Pay")
 *     }
 * }
 * ```
 *
 * ## Example: Use with PrimerCardForm component
 * ```kotlin
 * val cardFormState = rememberCardFormState(checkout)
 *
 * PrimerCardForm(
 *     state = cardFormState,
 *     submitButton = {
 *         // Custom submit button
 *         MyBrandButton(onClick = { cardFormState.submit() })
 *     }
 * )
 * ```
 *
 * @param checkout Checkout state from [io.primer.android.api.checkout.rememberPrimerCheckoutController]
 * @return Card form state and actions
 */
@Composable
fun rememberCardFormController(checkout: PrimerCheckoutController): PrimerCardFormController {
    val viewModel = viewModel<CardFormViewModel>(
        factory = CardFormViewModelFactory(),
    )

    // Register this flow as the active cleanable
    DisposableEffect(viewModel) {
        (checkout as? CheckoutViewModel)?.setActiveFlow(viewModel)
        onDispose { }
    }

    return viewModel
}
