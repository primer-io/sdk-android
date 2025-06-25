package io.primer.android.internal.presentation.screens.card

import androidx.lifecycle.viewModelScope
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.internal.domain.usecases.ManageCardFormUseCase
import io.primer.android.internal.domain.usecases.SubmitCardPaymentUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.internal.presentation.scope.DefaultCardFormScope
import io.primer.android.scope.PrimerCardFormScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class CardFormViewModel : DefaultCardFormScope(), DISdkComponent {

    private val manageCardFormUseCase: ManageCardFormUseCase by lazy { resolve() }
    private val submitCardPaymentUseCase: SubmitCardPaymentUseCase by lazy { resolve() }
    private val checkoutNavigator: CheckoutNavigator by lazy { resolve() }
    private val logReporter: LogReporter by lazy { resolve() }

    private val _uiState = MutableStateFlow<PrimerCardFormScope.State>(PrimerCardFormScope.State())
    override val state: StateFlow<PrimerCardFormScope.State> = _uiState.asStateFlow()

    init {
        // Initialize required fields
        viewModelScope.launch {
            val cardFields = manageCardFormUseCase.getCardFields()
            val billingFields = manageCardFormUseCase.getBillingFields()
            _uiState.value = PrimerCardFormScope.State(cardFields, billingFields)
        }

        // Collect form data
        manageCardFormUseCase.formData
            .onEach { formData ->
                _uiState.update { it.copy(inputFields = formData.toMap()) }
            }
            .launchIn(viewModelScope)

        // Collect validation errors
        manageCardFormUseCase.validationErrors
            .onEach { errors ->
                _uiState.update { it.copy(fieldErrors = errors) }
            }
            .launchIn(viewModelScope)

        // Collect card network states
        combine(
            manageCardFormUseCase.detectedCardNetwork,
            manageCardFormUseCase.selectedCardNetwork,
            manageCardFormUseCase.availableNetworks,
            manageCardFormUseCase.preferredNetwork
        ) { detectedNetwork, selectedNetwork, availableNetworks, preferredNetwork ->
            _uiState.update { currentState ->
                currentState.copy(
                    detectedCardNetwork = detectedNetwork,
                    selectedNetwork = selectedNetwork,
                    availableNetworks = availableNetworks,
                    preferredNetwork = preferredNetwork
                )
            }
        }.launchIn(viewModelScope)

        // Listen for country selection results
        checkoutNavigator.observeNavigationResult<Pair<String, String>>("selected_country")
            .filterNotNull()
            .onEach { (countryCode, countryName) ->
                manageCardFormUseCase.updateField(PrimerInputElementType.COUNTRY_CODE, countryCode)
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedCountry = PrimerCountry(
                            name = countryName,
                            code = CountryCode.safeValueOf(countryCode)
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun updateCardNumber(cardNumber: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.CARD_NUMBER, cardNumber)

    override fun updateCvv(cvv: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.CVV, cvv)

    override fun updateExpiryDate(expiryDate: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.EXPIRY_DATE, expiryDate)

    override fun updateCardholderName(cardholderName: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.CARDHOLDER_NAME, cardholderName)

    override fun updatePostalCode(postalCode: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.POSTAL_CODE, postalCode)

    override fun updateCountryCode(countryCode: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.COUNTRY_CODE, countryCode)

    override fun updateCity(city: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.CITY, city)

    override fun updateState(state: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.STATE, state)

    override fun updateAddressLine1(addressLine1: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.ADDRESS_LINE_1, addressLine1)

    override fun updateAddressLine2(addressLine2: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.ADDRESS_LINE_2, addressLine2)

    override fun updatePhoneNumber(phoneNumber: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.PHONE_NUMBER, phoneNumber)

    override fun updateFirstName(firstName: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.FIRST_NAME, firstName)

    override fun updateLastName(lastName: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.LAST_NAME, lastName)

    override fun updateRetailOutlet(retailOutlet: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.RETAIL_OUTLET, retailOutlet)

    override fun updateOtpCode(otpCode: String) =
        manageCardFormUseCase.updateField(PrimerInputElementType.OTP_CODE, otpCode)

    override fun onSubmit() {
        viewModelScope.launch {
            // Mark all required fields as dirty to trigger validation errors
            manageCardFormUseCase.markAllFieldsAsDirty()

            // Check if all validation passes before proceeding
            val isValidationPassed = manageCardFormUseCase.isSubmitAllowed.first()
            if (!isValidationPassed) {
                logReporter.debug("Validation failed, not proceeding with submission")
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            
            submitCardPaymentUseCase(manageCardFormUseCase.formData.first())
                .fold(
                    onSuccess = {
                        logReporter.debug("Payment completed successfully")
                        _uiState.update { it.copy(isLoading = false) }
                        checkoutNavigator.navigateToSuccess()
                    },
                    onFailure = { error ->
                        logReporter.error("Payment failed: ${error.message}")
                        _uiState.update { it.copy(isLoading = false) }
                        checkoutNavigator.navigateToError(error.message ?: "Payment failed")
                    }
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
        manageCardFormUseCase.selectCardNetwork(network)
}
