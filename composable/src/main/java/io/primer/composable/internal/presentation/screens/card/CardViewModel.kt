package io.primer.composable.internal.presentation.screens.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.logging.internal.LogReporter
import io.primer.composable.internal.domain.interactor.GetRequiredFieldsInteractor
import io.primer.composable.internal.domain.interactor.GetValidationStateInteractor
import io.primer.composable.internal.domain.interactor.SetCardDataInteractor
import io.primer.composable.internal.domain.interactor.SubmitPaymentInteractor
import io.primer.composable.internal.presentation.checkout.CheckoutNavigator
import io.primer.composable.internal.presentation.checkout.Screen
import io.primer.composable.internal.presentation.screens.card.DefaultCardFormScreen
import io.primer.composable.internal.presentation.screens.card.components.BillingAddressForm
import io.primer.composable.internal.presentation.screens.card.components.CardDetailsForm
import io.primer.composable.internal.presentation.screens.card.components.SubmitButton
import io.primer.composable.internal.presentation.screens.card.components.input.AddressLine1Input
import io.primer.composable.internal.presentation.screens.card.components.input.AddressLine2Input
import io.primer.composable.internal.presentation.screens.card.components.input.CardNumberInput
import io.primer.composable.internal.presentation.screens.card.components.input.CardholderNameInput
import io.primer.composable.internal.presentation.screens.card.components.input.CityInput
import io.primer.composable.internal.presentation.screens.card.components.input.CountryCodeInput
import io.primer.composable.internal.presentation.screens.card.components.input.CvvInput
import io.primer.composable.internal.presentation.screens.card.components.input.ExpiryDateInput
import io.primer.composable.internal.presentation.screens.card.components.input.FirstNameInput
import io.primer.composable.internal.presentation.screens.card.components.input.LastNameInput
import io.primer.composable.internal.presentation.screens.card.components.input.OtpCodeInput
import io.primer.composable.internal.presentation.screens.card.components.input.PhoneNumberInput
import io.primer.composable.internal.presentation.screens.card.components.input.PostalCodeInput
import io.primer.composable.internal.presentation.screens.card.components.input.RetailOutletInput
import io.primer.composable.internal.presentation.screens.card.components.input.StateInput
import io.primer.composable.scope.PrimerCardFormScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CardViewModel : ViewModel(), PrimerCardFormScope, DISdkComponent {

    private val getAvailableCardFieldsInteractor: GetRequiredFieldsInteractor by lazy { resolve() }
    private val setDataInteractor: SetCardDataInteractor by lazy { resolve() }
    private val getValidationStateInteractor: GetValidationStateInteractor by lazy { resolve() }
    private val submitPaymentInteractor: SubmitPaymentInteractor by lazy { resolve() }
    private val checkoutNavigator: CheckoutNavigator by lazy { resolve() }

    private val logReporter: LogReporter by lazy { resolve() }

    private val _uiState = MutableStateFlow<PrimerCardFormScope.State>(PrimerCardFormScope.State())
    override val state: StateFlow<PrimerCardFormScope.State> = _uiState.asStateFlow()

    // Default composable implementations (copied from CardFormScopeDefaults)
    override var PrimerCardFormScreen: @Composable () -> Unit = {
        DefaultCardFormScreen()
    }
    
    override var PrimerSubmitButton: @Composable (modifier: Modifier, text: String) -> Unit = { modifier, text ->
        SubmitButton(modifier = modifier, text = text)
    }
    
    override var PrimerCardNumberInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CardNumberInput(modifier)
    }
    
    override var PrimerCvvInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CvvInput(modifier)
    }
    
    override var PrimerExpiryDateInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        ExpiryDateInput(modifier)
    }
    
    override var PrimerCardholderNameInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CardholderNameInput(modifier)
    }
    
    override var PrimerPostalCodeInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        PostalCodeInput(modifier)
    }
    
    override var PrimerCountryCodeInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CountryCodeInput(modifier)
    }
    
    override var PrimerCityInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CityInput(modifier)
    }
    
    override var PrimerStateInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        StateInput(modifier)
    }
    
    override var PrimerAddressLine1Input: @Composable (modifier: Modifier) -> Unit = { modifier ->
        AddressLine1Input(modifier)
    }
    
    override var PrimerAddressLine2Input: @Composable (modifier: Modifier) -> Unit = { modifier ->
        AddressLine2Input(modifier)
    }
    
    override var PrimerPhoneNumberInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        PhoneNumberInput(modifier)
    }
    
    override var PrimerFirstNameInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        FirstNameInput(modifier)
    }
    
    override var PrimerLastNameInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        LastNameInput(modifier)
    }
    
    override var PrimerRetailOutletInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        RetailOutletInput(modifier)
    }
    
    override var PrimerOtpCodeInput: @Composable (modifier: Modifier) -> Unit = { modifier ->
        OtpCodeInput(modifier)
    }
    
    override var PrimerCardDetails: @Composable (modifier: Modifier) -> Unit = { modifier ->
        CardDetailsForm(modifier)
    }
    
    override var PrimerBillingAddress: @Composable (modifier: Modifier) -> Unit = { modifier ->
        BillingAddressForm(modifier)
    }

    init {
        viewModelScope.launch {
            val cardInputFields = getAvailableCardFieldsInteractor.getCardFields()
            val billingInputFields = getAvailableCardFieldsInteractor.getBillingFields()
            _uiState.value = PrimerCardFormScope.State(cardInputFields, billingInputFields)
        }

        viewModelScope.launch {
            setDataInteractor.inputData.collect { input ->
                _uiState.update {
                    it.copy(inputFields = input)
                }
            }
        }

        viewModelScope.launch {
            getValidationStateInteractor.validationState.collect { errors ->
                _uiState.update {
                    it.copy(fieldErrors = errors)
                }
            }
        }

        viewModelScope.launch {
            getValidationStateInteractor.isSubmitAllowed.collect { isAllowed ->
                _uiState.update {
                    it.copy(isSubmitEnabled = isAllowed)
                }
            }
        }
    }

    override fun updateCardNumber(cardNumber: String) {
        setDataInteractor.updateInput(cardNumber, PrimerInputElementType.CARD_NUMBER)
    }

    override fun updateCvv(cvv: String) {
        setDataInteractor.updateInput(cvv, PrimerInputElementType.CVV)
    }

    override fun updateExpiryDate(expiryDate: String) {
        setDataInteractor.updateInput(expiryDate, PrimerInputElementType.EXPIRY_DATE)
    }

    override fun updateCardholderName(cardholderName: String) {
        setDataInteractor.updateInput(cardholderName, PrimerInputElementType.CARDHOLDER_NAME)
    }

    override fun updatePostalCode(postalCode: String) {
        setDataInteractor.updateInput(postalCode, PrimerInputElementType.POSTAL_CODE)
    }

    override fun updateCountryCode(countryCode: String) {
        setDataInteractor.updateInput(countryCode, PrimerInputElementType.COUNTRY_CODE)
    }

    override fun updateCity(city: String) {
        setDataInteractor.updateInput(city, PrimerInputElementType.CITY)
    }

    override fun updateState(state: String) {
        setDataInteractor.updateInput(state, PrimerInputElementType.STATE)
    }

    override fun updateAddressLine1(addressLine1: String) {
        setDataInteractor.updateInput(addressLine1, PrimerInputElementType.ADDRESS_LINE_1)
    }

    override fun updateAddressLine2(addressLine2: String) {
        setDataInteractor.updateInput(addressLine2, PrimerInputElementType.ADDRESS_LINE_2)
    }

    override fun updatePhoneNumber(phoneNumber: String) {
        setDataInteractor.updateInput(phoneNumber, PrimerInputElementType.PHONE_NUMBER)
    }

    override fun updateFirstName(firstName: String) {
        setDataInteractor.updateInput(firstName, PrimerInputElementType.FIRST_NAME)
    }

    override fun updateLastName(lastName: String) {
        setDataInteractor.updateInput(lastName, PrimerInputElementType.LAST_NAME)
    }

    override fun updateRetailOutlet(retailOutlet: String) {
        setDataInteractor.updateInput(retailOutlet, PrimerInputElementType.RETAIL_OUTLET)
    }

    override fun updateOtpCode(otpCode: String) {
        setDataInteractor.updateInput(otpCode, PrimerInputElementType.OTP_CODE)
    }

    override fun onSubmit() {
        viewModelScope.launch {
            checkoutNavigator.navigateTo(Screen.Success)
        }

//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true, isSubmitEnabled = false)
//            try {
//                val result = submitPaymentInteractor(_uiState.value.inputFields)
//                result.fold(
//                    onSuccess = { checkoutData ->
//                        logReporter.debug("Payment completed successfully")
//                        _uiState.value = _uiState.value.copy(isLoading = false, isSubmitEnabled = true)
//                    },
//                    onFailure = { error ->
//                        logReporter.error("Payment failed: ${error.message}")
//                        _uiState.value = _uiState.value.copy(isLoading = false, isSubmitEnabled = true)
//                    },
//                )
//            } catch (e: Exception) {
//                logReporter.error("Payment submission failed: ${e.message}")
//                _uiState.value = _uiState.value.copy(isLoading = false, isSubmitEnabled = true)
//            }
//        }
    }

    override fun onBack() {
        viewModelScope.launch {
            checkoutNavigator.navigateBack()
        }
    }

    override fun onCancel() {
        viewModelScope.launch {
            checkoutNavigator.dismiss()
        }
    }
}
