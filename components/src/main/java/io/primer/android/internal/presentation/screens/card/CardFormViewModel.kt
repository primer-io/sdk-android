package io.primer.android.internal.presentation.screens.card

import androidx.lifecycle.viewModelScope
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.internal.domain.interactor.CardNetworkInteractor
import io.primer.android.internal.domain.interactor.GetRequiredFieldsInteractor
import io.primer.android.internal.domain.interactor.GetValidationStateInteractor
import io.primer.android.internal.domain.interactor.SetCardDataInteractor
import io.primer.android.internal.domain.interactor.SubmitPaymentInteractor
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.internal.presentation.scope.DefaultCardFormScope
import io.primer.android.scope.PrimerCardFormScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CardFormViewModel : DefaultCardFormScope(), DISdkComponent {

    private val getAvailableCardFieldsInteractor: GetRequiredFieldsInteractor by lazy { resolve() }
    private val setDataInteractor: SetCardDataInteractor by lazy { resolve() }
    private val getValidationStateInteractor: GetValidationStateInteractor by lazy { resolve() }
    private val submitPaymentInteractor: SubmitPaymentInteractor by lazy { resolve() }
    private val checkoutNavigator: CheckoutNavigator by lazy { resolve() }
    private val cardNetworkInteractor: CardNetworkInteractor by lazy { resolve() }

    private val logReporter: LogReporter by lazy { resolve() }

    private val _uiState = MutableStateFlow<PrimerCardFormScope.State>(PrimerCardFormScope.State())
    override val state: StateFlow<PrimerCardFormScope.State> = _uiState.asStateFlow()

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
            cardNetworkInteractor.detectedCardNetwork.collect { detectedNetwork ->
                _uiState.update {
                    it.copy(detectedCardNetwork = detectedNetwork)
                }
                setDataInteractor.updateCardNetwork(detectedNetwork)
            }
        }

        viewModelScope.launch {
            cardNetworkInteractor.availableNetworks.collect { networks ->
                _uiState.update {
                    it.copy(availableNetworks = networks)
                }
            }
        }

        viewModelScope.launch {
            cardNetworkInteractor.selectedNetwork.collect { selectedNetwork ->
                _uiState.update {
                    it.copy(selectedNetwork = selectedNetwork)
                }
                // Update SetCardDataInteractor with the effective network
                val effectiveNetwork = selectedNetwork ?: cardNetworkInteractor.getCardNetwork()
                setDataInteractor.updateCardNetwork(effectiveNetwork)
            }
        }

        viewModelScope.launch {
            cardNetworkInteractor.preferredNetwork.collect { preferredNetwork ->
                _uiState.update {
                    it.copy(preferredNetwork = preferredNetwork)
                }
            }
        }

        // Listen for country selection results
        viewModelScope.launch {
            checkoutNavigator.observeNavigationResult<Pair<String, String>>("selected_country")
                .filterNotNull()
                .collect { (countryCode, countryName) ->
                    updateCountryCode(countryCode)
                    _uiState.update { currentState ->
                        currentState.copy(
                            selectedCountry = PrimerCountry(
                                name = countryName,
                                code = CountryCode.safeValueOf(countryCode)
                            )
                        )
                    }
                }
        }
    }

    override fun updateCardNumber(cardNumber: String) {
        setDataInteractor.updateInput(cardNumber, PrimerInputElementType.CARD_NUMBER)
        cardNetworkInteractor.updateDetectedCardNetwork(cardNumber)
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
            // Mark all required fields as dirty to trigger validation errors
            val allRequiredFields = getAvailableCardFieldsInteractor.getCardFields() + 
                                   getAvailableCardFieldsInteractor.getBillingFields()
            allRequiredFields.forEach { field ->
                setDataInteractor.markFieldAsDirty(field)
            }

            // Check if all validation passes before proceeding
            val isValidationPassed = getValidationStateInteractor.isSubmitAllowed.first()

            if (!isValidationPassed) {
                logReporter.debug("Validation failed, not proceeding with submission")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = submitPaymentInteractor(_uiState.value.inputFields)
                result.fold(
                    onSuccess = { checkoutData ->
                        logReporter.debug("Payment completed successfully")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        checkoutNavigator.navigateToSuccess()
                    },
                    onFailure = { error ->
                        logReporter.error("Payment failed: ${error.message}")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        checkoutNavigator.navigateToError(error.message ?: "Payment failed")
                    },
                )
            } catch (e: Exception) {
                logReporter.error("Payment submission failed: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
                checkoutNavigator.navigateToError(e.message ?: "Payment submission failed")
            }
        }
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

    override fun navigateToCountrySelection() {
        viewModelScope.launch {
            checkoutNavigator.navigateTo(Screen.SelectCountry)
        }
    }

    override fun selectCardNetwork(network: CardNetwork.Type) {
        cardNetworkInteractor.selectNetwork(network)
    }
}
