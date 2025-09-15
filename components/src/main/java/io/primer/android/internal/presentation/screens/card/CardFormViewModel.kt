package io.primer.android.internal.presentation.screens.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.internal.domain.usecase.CardFieldsUseCase
import io.primer.android.internal.domain.usecase.CardNetworkUseCase
import io.primer.android.internal.domain.usecase.SubmitCardPaymentUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.scope.PrimerCardFormScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class CardFormViewModel(
    private val cardFieldsUseCase: CardFieldsUseCase,
    private val cardNetworkUseCase: CardNetworkUseCase,
    private val submitCardPaymentUseCase: SubmitCardPaymentUseCase,
    private val checkoutNavigator: CheckoutNavigator,
    private val logReporter: LogReporter,
) : ViewModel(), PrimerCardFormScope {

    private val _uiState = MutableStateFlow(PrimerCardFormScope.State())
    override val state: StateFlow<PrimerCardFormScope.State> = _uiState.asStateFlow()

    init {
        // Initialize required fields
        viewModelScope.launch {
            val cardFields = cardFieldsUseCase.getCardFields()
            val billingFields = cardFieldsUseCase.getBillingFields()
            _uiState.value = PrimerCardFormScope.State(cardFields, billingFields)
        }

        // Collect form data
        cardFieldsUseCase.formData
            .onEach { formData ->
                _uiState.update { it.copy(data = formData) }
            }
            .launchIn(viewModelScope)

        // Collect validation errors
        cardFieldsUseCase.validationErrors
            .onEach { errors ->
                _uiState.update { it.copy(fieldErrors = errors) }
            }
            .launchIn(viewModelScope)

        // Collect field focus states
        cardFieldsUseCase.fieldFocusStates
            .onEach { focusStates ->
                _uiState.update { it.copy(fieldFocusStates = focusStates) }
            }
            .launchIn(viewModelScope)

        // Collect form validity state
        cardFieldsUseCase.isFormValid
            .onEach { isValid ->
                _uiState.update { it.copy(isFormValid = isValid) }
            }
            .launchIn(viewModelScope)

        // Collect current card network immediately
        cardNetworkUseCase.currentCardNetwork
            .onEach { currentCardNetwork ->
                cardFieldsUseCase.updateCardNetwork(currentCardNetwork)

                _uiState.update { currentState ->
                    currentState.copy(
                        selectedNetwork = currentCardNetwork,
                    )
                }
            }
            .launchIn(viewModelScope)

        // Collect available networks separately (slower)
        cardNetworkUseCase.availableNetworks
            .onEach { availableNetworks ->
                _uiState.update { currentState ->
                    currentState.copy(
                        availableNetworks = availableNetworks,
                    )
                }
            }
            .launchIn(viewModelScope)

        // Listen for country selection results
        checkoutNavigator.observeNavigationResult<Pair<String, String>>("selected_country")
            .filterNotNull()
            .onEach { (countryCode, countryName) ->
                cardFieldsUseCase.updateField(PrimerInputElementType.COUNTRY_CODE, countryCode)
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedCountry = PrimerCountry(
                            name = countryName,
                            code = CountryCode.safeValueOf(countryCode),
                        ),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun updateCardNumber(cardNumber: String) {
        if (cardNumber.isEmpty()) {
            cardNetworkUseCase.clear()
        }
        cardFieldsUseCase.updateField(PrimerInputElementType.CARD_NUMBER, cardNumber)
        cardNetworkUseCase.detectCardNetwork(cardNumber)
    }

    override fun updateCvv(cvv: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.CVV, cvv)

    override fun updateExpiryDate(expiryDate: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.EXPIRY_DATE, expiryDate)

    override fun updateCardholderName(cardholderName: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.CARDHOLDER_NAME, cardholderName)

    override fun updatePostalCode(postalCode: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.POSTAL_CODE, postalCode)

    override fun updateCountryCode(countryCode: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.COUNTRY_CODE, countryCode)

    override fun updateCity(city: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.CITY, city)

    override fun updateState(state: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.STATE, state)

    override fun updateAddressLine1(addressLine1: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.ADDRESS_LINE_1, addressLine1)

    override fun updateAddressLine2(addressLine2: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.ADDRESS_LINE_2, addressLine2)

    override fun updatePhoneNumber(phoneNumber: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.PHONE_NUMBER, phoneNumber)

    override fun updateFirstName(firstName: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.FIRST_NAME, firstName)

    override fun updateLastName(lastName: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.LAST_NAME, lastName)

    override fun updateRetailOutlet(retailOutlet: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.RETAIL_OUTLET, retailOutlet)

    override fun updateOtpCode(otpCode: String) =
        cardFieldsUseCase.updateField(PrimerInputElementType.OTP_CODE, otpCode)

    override fun onSubmit() {
        viewModelScope.launch {
            cardFieldsUseCase.markSubmitAttempted()
            if (!cardFieldsUseCase.isSubmitAllowed()) {
                logReporter.debug("Validation failed, not proceeding with submission")
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, isFormEnabled = false) }

            submitCardPaymentUseCase(cardFieldsUseCase.formData.first())
                .fold(
                    onSuccess = {
                        logReporter.debug("Payment completed successfully")
                        _uiState.update { it.copy(isLoading = false, isFormEnabled = true) }
                        checkoutNavigator.navigateToSuccess()
                    },
                    onFailure = { error ->
                        logReporter.error("Payment failed: ${error.message}")
                        _uiState.update { it.copy(isLoading = false, isFormEnabled = true) }
                        checkoutNavigator.navigateToError(error.message ?: "Payment failed")
                    },
                )
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

    override fun selectCardNetwork(network: CardNetwork.Type) =
        cardNetworkUseCase.selectCardNetwork(network)

    override fun onFieldFocusChange(field: PrimerInputElementType, hasFocus: Boolean) {
        cardFieldsUseCase.onFieldFocusChange(field, hasFocus)
    }
}
