package io.primer.android.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.ui.core.model.SyncValidationError
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the scope for Primer's card form functionality, providing state management,
 * field updates, and UI customization for card payment input.
 */
interface PrimerCardFormScope {

    /**
     * StateFlow representing the current state of the card form, including field data,
     * validation errors, loading states, and selected options.
     */
    val state: StateFlow<State>

    /**
     * Scope for country selection functionality within the card form.
     * Used for billing address country selection.
     */
    val selectCountry: PrimerSelectCountryScope

    /**
     * Initializes the card form with configuration and prepares it for user input.
     * Should be called before displaying the form to the user.
     */
    fun init()

    /**
     * Updates the card number field value and triggers validation.
     *
     * @param cardNumber The card number string to update
     */
    fun updateCardNumber(cardNumber: String)

    /**
     * Updates the CVV field value and triggers validation.
     *
     * @param cvv The CVV string to update
     */
    fun updateCvv(cvv: String)

    /**
     * Updates the expiry date field value and triggers validation.
     *
     * @param expiryDate The expiry date string to update (format: MM/YY)
     */
    fun updateExpiryDate(expiryDate: String)

    /**
     * Updates the cardholder name field value and triggers validation.
     *
     * @param cardholderName The cardholder name string to update
     */
    fun updateCardholderName(cardholderName: String)

    /**
     * Updates the postal code field value and triggers validation.
     *
     * @param postalCode The postal code string to update
     */
    fun updatePostalCode(postalCode: String)

    /**
     * Updates the country code field value and triggers validation.
     *
     * @param countryCode The country code string to update
     */
    fun updateCountryCode(countryCode: String)

    /**
     * Updates the city field value and triggers validation.
     *
     * @param city The city string to update
     */
    fun updateCity(city: String)

    /**
     * Updates the state/province field value and triggers validation.
     *
     * @param state The state/province string to update
     */
    fun updateState(state: String)

    /**
     * Updates the first address line field value and triggers validation.
     *
     * @param addressLine1 The first address line string to update
     */
    fun updateAddressLine1(addressLine1: String)

    /**
     * Updates the second address line field value and triggers validation.
     *
     * @param addressLine2 The second address line string to update
     */
    fun updateAddressLine2(addressLine2: String)

    /**
     * Updates the phone number field value and triggers validation.
     *
     * @param phoneNumber The phone number string to update
     */
    fun updatePhoneNumber(phoneNumber: String)

    /**
     * Updates the first name field value and triggers validation.
     *
     * @param firstName The first name string to update
     */
    fun updateFirstName(firstName: String)

    /**
     * Updates the last name field value and triggers validation.
     *
     * @param lastName The last name string to update
     */
    fun updateLastName(lastName: String)

    /**
     * Updates the retail outlet field value and triggers validation.
     *
     * @param retailOutlet The retail outlet string to update
     */
    fun updateRetailOutlet(retailOutlet: String)

    /**
     * Updates the OTP code field value and triggers validation.
     *
     * @param otpCode The OTP code string to update
     */
    fun updateOtpCode(otpCode: String)

    /**
     * Submits the card form for payment processing.
     * Validates all required fields and initiates the payment flow.
     */
    fun onSubmit()

    /**
     * Navigates back to the previous screen in the card form flow.
     */
    fun onBack()

    /**
     * Cancels the whole checkout flow.
     */
    fun onCancel()

    /**
     * Navigates to the country selection screen for billing address input.
     */
    fun navigateToCountrySelection()

    /**
     * Selects a specific card network for co-badged cards.
     *
     * @param network The card network type to select
     */
    fun selectCardNetwork(network: CardNetwork.Type)

    /**
     * Represents the current state of the card form, including field configurations,
     * user input data, validation errors, and UI state.
     *
     * @param cardFields List of card-related input fields to display
     * @param billingFields List of billing address input fields to display
     * @param fieldErrors List of validation errors for form fields
     * @param data Map of field types to their current string values
     * @param isLoading Whether the form is in a loading state
     * @param selectedCountry Currently selected country for billing address
     * @param selectedNetwork Currently selected card network
     * @param availableNetworks List of available card networks for selection
     */
    data class State(
        val cardFields: List<PrimerInputElementType> = emptyList(),
        val billingFields: List<PrimerInputElementType> = emptyList(),
        val fieldErrors: List<SyncValidationError>? = emptyList(),
        val data: Map<PrimerInputElementType, String> = emptyMap(),
        val isLoading: Boolean = false,
        val selectedCountry: PrimerCountry? = null,
        val selectedNetwork: CardNetwork.Type = CardNetwork.Type.OTHER,
        val availableNetworks: List<PrimerCardNetwork> = emptyList(),
    )

    /**
     * Composable function for the entire card form screen layout.
     */
    var screen: @Composable () -> Unit

    /**
     * Composable function for the submit button with customizable styling and text.
     *
     * @param modifier Modifier for styling the button
     * @param text Text to display on the button
     */
    var submitButton: @Composable (modifier: Modifier, text: String) -> Unit

    /**
     * Composable function for the card number input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var cardNumberInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the CVV input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var cvvInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the expiry date input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var expiryDateInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the cardholder name input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var cardholderNameInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the postal code input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var postalCodeInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the country code input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var countryCodeInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the city input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var cityInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the state/province input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var stateInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the first address line input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var addressLine1Input: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the second address line input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var addressLine2Input: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the phone number input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var phoneNumberInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the first name input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var firstNameInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the last name input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var lastNameInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the retail outlet input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var retailOutletInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for the OTP code input field with validation.
     *
     * @param modifier Modifier for styling the input field
     */
    var otpCodeInput: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for displaying grouped card details section.
     *
     * @param modifier Modifier for styling the section
     */
    var cardDetails: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for displaying grouped billing address section.
     *
     * @param modifier Modifier for styling the section
     */
    var billingAddress: @Composable (modifier: Modifier) -> Unit

    /**
     * Composable function for displaying card network selection interface.
     *
     * @param modifier Modifier for styling the network selector
     */
    var cardNetwork: @Composable (modifier: Modifier) -> Unit
}
