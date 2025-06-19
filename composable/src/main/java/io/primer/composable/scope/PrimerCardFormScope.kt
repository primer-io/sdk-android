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

    var screen: @Composable () -> Unit
    var submitButton: @Composable (modifier: Modifier, text: String) -> Unit
    var cardNumberInput: @Composable (modifier: Modifier) -> Unit
    var cvvInput: @Composable (modifier: Modifier) -> Unit
    var expiryDateInput: @Composable (modifier: Modifier) -> Unit
    var cardholderNameInput: @Composable (modifier: Modifier) -> Unit
    var postalCodeInput: @Composable (modifier: Modifier) -> Unit
    var countryCodeInput: @Composable (modifier: Modifier) -> Unit
    var cityInput: @Composable (modifier: Modifier) -> Unit
    var stateInput: @Composable (modifier: Modifier) -> Unit
    var addressLine1Input: @Composable (modifier: Modifier) -> Unit
    var addressLine2Input: @Composable (modifier: Modifier) -> Unit
    var phoneNumberInput: @Composable (modifier: Modifier) -> Unit
    var firstNameInput: @Composable (modifier: Modifier) -> Unit
    var lastNameInput: @Composable (modifier: Modifier) -> Unit
    var retailOutletInput: @Composable (modifier: Modifier) -> Unit
    var otpCodeInput: @Composable (modifier: Modifier) -> Unit
    var cardDetails: @Composable (modifier: Modifier) -> Unit
    var billingAddress: @Composable (modifier: Modifier) -> Unit
}
