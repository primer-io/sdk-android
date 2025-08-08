package io.primer.android.scope

import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.PrimerCardFormComponents
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.ui.core.model.SyncValidationError
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the scope for Primer's card form functionality, providing state management,
 * field updates, and UI customization for card payment input.
 */
interface PrimerCardFormScope: DISdkComponent {

    val components: PrimerCardFormComponents
        get() = resolve()

    /**
     * StateFlow representing the current state of the card form, including field data,
     * validation errors, loading states, and selected options.
     */
    val state: StateFlow<State>

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
}
