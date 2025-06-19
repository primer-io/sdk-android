package io.primer.composable.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import kotlinx.coroutines.flow.StateFlow

interface PrimerCardFormScope {

    val state: StateFlow<State>

    fun updateCardNumber(cardNumber: String)
    fun updateCvv(cvv: String)
    fun updateExpiryDate(expiryDate: String)
    fun updateCardholderName(cardholderName: String)
    fun updatePostalCode(postalCode: String)
    fun updateCountryCode(countryCode: String)
    fun updateCity(city: String)
    fun updateState(state: String)
    fun updateAddressLine1(addressLine1: String)
    fun updateAddressLine2(addressLine2: String)
    fun updatePhoneNumber(phoneNumber: String)
    fun updateFirstName(firstName: String)
    fun updateLastName(lastName: String)
    fun updateRetailOutlet(retailOutlet: String)
    fun updateOtpCode(otpCode: String)

    fun onSubmit()

    // TODO COMPOSABLE maybe this can be removed and trigger OS back button
    fun onBack()

    fun onCancel()

    data class State(
        val cardFields: List<PrimerInputElementType> = emptyList(),
        val billingFields: List<PrimerInputElementType> = emptyList(),
        val fieldErrors: List<PrimerInputValidationError> = emptyList(),
        val inputFields: Map<PrimerInputElementType, String> = emptyMap(),
        val isLoading: Boolean = false,
        val isSubmitEnabled: Boolean = false,
    )

    // Non-nullable composable properties (replacing companion object extensions)
    var PrimerCardFormScreen: @Composable () -> Unit
    var PrimerSubmitButton: @Composable (modifier: Modifier, text: String) -> Unit
    var PrimerCardNumberInput: @Composable (modifier: Modifier) -> Unit
    var PrimerCvvInput: @Composable (modifier: Modifier) -> Unit
    var PrimerExpiryDateInput: @Composable (modifier: Modifier) -> Unit
    var PrimerCardholderNameInput: @Composable (modifier: Modifier) -> Unit
    var PrimerPostalCodeInput: @Composable (modifier: Modifier) -> Unit
    var PrimerCountryCodeInput: @Composable (modifier: Modifier) -> Unit
    var PrimerCityInput: @Composable (modifier: Modifier) -> Unit
    var PrimerStateInput: @Composable (modifier: Modifier) -> Unit
    var PrimerAddressLine1Input: @Composable (modifier: Modifier) -> Unit
    var PrimerAddressLine2Input: @Composable (modifier: Modifier) -> Unit
    var PrimerPhoneNumberInput: @Composable (modifier: Modifier) -> Unit
    var PrimerFirstNameInput: @Composable (modifier: Modifier) -> Unit
    var PrimerLastNameInput: @Composable (modifier: Modifier) -> Unit
    var PrimerRetailOutletInput: @Composable (modifier: Modifier) -> Unit
    var PrimerOtpCodeInput: @Composable (modifier: Modifier) -> Unit
    var PrimerCardDetails: @Composable (modifier: Modifier) -> Unit
    var PrimerBillingAddress: @Composable (modifier: Modifier) -> Unit
}
