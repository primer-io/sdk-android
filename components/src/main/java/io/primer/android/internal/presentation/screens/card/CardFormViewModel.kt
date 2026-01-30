package io.primer.android.internal.presentation.screens.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.api.components.card.PrimerCardFormController
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.errors.domain.models.PrimerUnknownError
import io.primer.android.internal.domain.Cleanable
import io.primer.android.internal.domain.error.PrimerErrorException
import io.primer.android.internal.domain.usecase.CardFieldsUseCase
import io.primer.android.internal.domain.usecase.CardFormCleanupUseCase
import io.primer.android.internal.domain.usecase.CardNetworkUseCase
import io.primer.android.internal.domain.usecase.SetVaultOnSuccessUseCase
import io.primer.android.internal.domain.usecase.SubmitCardPaymentUseCase
import io.primer.android.internal.navigation.CheckoutResultHandler
import io.primer.android.internal.navigation.CountryNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions", "LongParameterList")
internal class CardFormViewModel(
    private val cardFieldsUseCase: CardFieldsUseCase,
    private val cardNetworkUseCase: CardNetworkUseCase,
    private val submitCardPaymentUseCase: SubmitCardPaymentUseCase,
    private val setVaultOnSuccessUseCase: SetVaultOnSuccessUseCase,
    private val cleanupUseCase: CardFormCleanupUseCase,
    private val logReporter: LogReporter,
    private val componentsEventsRepository: ComponentsEventsRepository,
    private val countryNavigator: CountryNavigator,
    private val resultHandler: CheckoutResultHandler,
) : ViewModel(), PrimerCardFormController, Cleanable {

    private val _uiState = MutableStateFlow(PrimerCardFormController.State())
    override val state: StateFlow<PrimerCardFormController.State> = _uiState.asStateFlow()
    private var hasEnteredAllDetails = false

    init {
        logReporter.info("Initializing card form", component = TAG)

        // Listen for country selection results
        countryNavigator.countrySelectionResult
            .onEach { result ->
                onCountrySelected(result.code, result.name)
            }
            .launchIn(viewModelScope)

        // Initialize required fields
        viewModelScope.launch {
            val cardFields = cardFieldsUseCase.getCardFields()
            val billingFields = cardFieldsUseCase.getBillingFields()
            logReporter.debug(
                "Card fields initialized: cardFields=${cardFields.size}, billingFields=${billingFields.size}",
                component = TAG,
            )
            _uiState.value = PrimerCardFormController.State(cardFields, billingFields)
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
                // Check if all required fields are valid and send PAYMENT_DETAILS_ENTERED event
                if (errors.isEmpty() && !hasEnteredAllDetails) {
                    viewModelScope.launch {
                        if (cardFieldsUseCase.isSubmitAllowed()) {
                            hasEnteredAllDetails = true
                            componentsEventsRepository.send(EventType.PaymentDetailsEntered.card())
                        }
                    }
                }
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

        // Collect network selection state
        cardNetworkUseCase.networkSelection
            .onEach { networkSelection ->
                cardFieldsUseCase.updateCardNetwork(
                    networkSelection.selectedNetwork.takeIf { networkSelection.isNetworkSelectable },
                )
                _uiState.update { it.copy(networkSelection = networkSelection) }
            }
            .launchIn(viewModelScope)
    }

    private fun onCountrySelected(countryCode: String, countryName: String) {
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

    override fun requestCountrySelection() {
        countryNavigator.navigateToCountrySelection()
    }

    override fun updateCardNumber(cardNumber: String) {
        cardFieldsUseCase.updateField(PrimerInputElementType.CARD_NUMBER, cardNumber)
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

    override fun submit() {
        logReporter.debug("Submit button tapped", component = TAG)
        viewModelScope.launch {
            componentsEventsRepository.send(EventType.PaymentSubmitted.card())
            cardFieldsUseCase.markSubmitAttempted()
            if (!cardFieldsUseCase.isSubmitAllowed()) {
                logReporter.debug("Validation failed, not proceeding with submission", component = TAG)
                return@launch
            }

            logReporter.info("Payment submission started", component = TAG)
            _uiState.update { it.copy(isLoading = true, isFormEnabled = false) }
            componentsEventsRepository.send(EventType.PaymentProcessingStarted.card())

            submitCardPaymentUseCase(cardFieldsUseCase.formData.first())
                .fold(
                    onSuccess = { checkoutData ->
                        logReporter.info("Payment completed successfully", component = TAG)
                        _uiState.update { it.copy(isLoading = false, isFormEnabled = true) }
                        componentsEventsRepository.send(
                            EventType.PaymentSuccess.card(checkoutData.payment.id),
                        )
                        resultHandler.onSuccess(checkoutData)
                    },
                    onFailure = { error ->
                        logReporter.error(
                            "Payment failed: ${error.message}",
                            component = TAG,
                            throwable = error,
                        )
                        _uiState.update { it.copy(isLoading = false, isFormEnabled = true) }
                        componentsEventsRepository.send(EventType.PaymentFailure.card())
                        val primerError = (error as? PrimerErrorException)?.primerError
                            ?: PrimerUnknownError(error.message ?: "Payment failed")
                        resultHandler.onError(primerError)
                    },
                )
        }
    }

    override fun selectCardNetwork(network: PrimerCardNetwork) =
        cardNetworkUseCase.selectCardNetwork(network)

    override fun onFieldFocusChange(field: PrimerInputElementType, hasFocus: Boolean) {
        cardFieldsUseCase.onFieldFocusChange(field, hasFocus)
    }

    override fun setVaultOnSuccess(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }
            setVaultOnSuccessUseCase(enabled)
                .fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                vaultOnSuccess = enabled,
                                isLoading = false,
                            )
                        }
                    },
                    onFailure = { throwable ->
                        logReporter.warn(
                            "Failed to set vaultOnSuccess=$enabled: ${throwable.message}",
                            component = TAG,
                        )
                        _uiState.update {
                            it.copy(
                                vaultOnSuccess = !enabled,
                                isLoading = false,
                            )
                        }
                    },
                )
        }
    }

    override fun cleanup() {
        cleanupUseCase.cleanup()
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    companion object {
        private const val TAG = "CardFormViewModel"
    }
}
