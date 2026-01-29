package io.primer.android.internal.presentation.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.domain.usecase.vault.CheckCvvRecaptureRequiredUseCase
import io.primer.android.internal.domain.usecase.vault.DeleteVaultedPaymentMethodUseCase
import io.primer.android.internal.domain.usecase.vault.FetchVaultedPaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.vault.SubmitVaultedPaymentUseCase
import io.primer.android.scope.PrimerVaultScope
import io.primer.android.scope.PrimerVaultScope.State
import io.primer.cardShared.CardNumberFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class VaultViewModel(
    private val fetchVaultedPaymentMethodsUseCase: FetchVaultedPaymentMethodsUseCase,
    private val submitVaultedPaymentUseCase: SubmitVaultedPaymentUseCase,
    private val checkCvvRecaptureRequiredUseCase: CheckCvvRecaptureRequiredUseCase,
    private val deleteVaultedPaymentMethodUseCase: DeleteVaultedPaymentMethodUseCase,
    private val componentsEventsRepository: ComponentsEventsRepository,
    private val logReporter: LogReporter,
) : ViewModel(), PrimerVaultScope {

    sealed interface NavigationEvent {
        data object NavigateToSelectedMethod : NavigationEvent
        data object ShowDeleteConfirmation : NavigationEvent
        data class ShowCvvRecapture(val paymentMethodId: String) : NavigationEvent
        data object NavigateToLoading : NavigationEvent
        data class PaymentSuccess(val checkoutData: PrimerCheckoutData) : NavigationEvent
        data class PaymentError(val error: Throwable) : NavigationEvent
        data object DeleteSuccess : NavigationEvent
    }

    private val _navigation = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()

    private val _state = MutableStateFlow(State())
    override val state: StateFlow<State> = _state.asStateFlow()

    init {
        logReporter.info("Initializing vault", component = TAG)
        viewModelScope.launch {
            fetchVaultedPaymentMethodsUseCase().fold(
                onSuccess = { methods ->
                    logReporter.debug(
                        "Vaulted payment methods fetched: ${methods.size} methods",
                        component = TAG,
                    )
                    _state.update {
                        it.copy(
                            paymentMethods = methods,
                            selectedPaymentMethod = methods.firstOrNull(),
                        )
                    }
                },
                onFailure = { exception ->
                    logReporter.error(
                        "Failed to fetch vaulted payment methods: ${exception.message}",
                        component = TAG,
                        throwable = exception,
                    )
                    _state.update { it.copy(error = exception) }
                },
            )
        }
    }

    override fun selectForPayment(paymentMethod: PrimerVaultedPaymentMethod) {
        logReporter.debug(
            "Vaulted payment method selected: type=${paymentMethod.paymentMethodType}",
            component = TAG,
        )
        _state.update {
            it.copy(
                selectedPaymentMethod = paymentMethod,
                cvv = null,
                error = null,
            )
        }
        viewModelScope.launch {
            _navigation.send(NavigationEvent.NavigateToSelectedMethod)
        }
    }

    override fun selectForDeletion(paymentMethod: PrimerVaultedPaymentMethod) {
        logReporter.debug(
            "Vaulted payment method selected for deletion: type=${paymentMethod.paymentMethodType}",
            component = TAG,
        )
        _state.update { it.copy(selectedPaymentMethod = paymentMethod, error = null) }
        viewModelScope.launch {
            _navigation.send(NavigationEvent.ShowDeleteConfirmation)
        }
    }

    override fun updateCvv(cvv: String) {
        val first6 = _state.value.selectedPaymentMethod?.paymentInstrumentData?.first6Digits?.toString().orEmpty()
        val expectedLength = CardNumberFormatter.fromString(first6).getCvvLength()
        val isValid = cvv.length == expectedLength && cvv.all { it.isDigit() }
        _state.update { it.copy(cvv = PrimerVaultScope.Cvv(cvv, isValid)) }
    }

    override fun submit() {
        logReporter.debug("Submit button tapped", component = TAG)
        val currentState = _state.value
        val paymentMethod = currentState.selectedPaymentMethod ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val needsCvv = checkCvvRecaptureRequiredUseCase(paymentMethod)
            if (needsCvv && currentState.cvv?.isValid != true) {
                logReporter.debug("CVV recapture required", component = TAG)
                _navigation.send(NavigationEvent.ShowCvvRecapture(paymentMethod.id))
                return@launch
            }
            _navigation.send(NavigationEvent.NavigateToLoading)
            logReporter.info(
                "Vault payment submission started: type=${paymentMethod.paymentMethodType}",
                component = TAG,
            )
            componentsEventsRepository.send(EventType.PaymentSubmitted(paymentMethod.paymentMethodType))
            submitVaultedPaymentUseCase(paymentMethod.id, currentState.cvv?.value).fold(
                onSuccess = { checkoutData ->
                    logReporter.info("Vault payment completed successfully", component = TAG)
                    componentsEventsRepository.send(
                        EventType.PaymentSuccess(paymentMethod.paymentMethodType, paymentMethod.id),
                    )
                    _state.update { it.copy(cvv = null, isLoading = false, error = null) }
                    _navigation.send(NavigationEvent.PaymentSuccess(checkoutData))
                },
                onFailure = { error ->
                    logReporter.error(
                        "Vault payment failed: ${error.message}",
                        component = TAG,
                        throwable = error,
                    )
                    componentsEventsRepository.send(EventType.PaymentFailure(paymentMethod.paymentMethodType))
                    _state.update { it.copy(isLoading = false, error = error) }
                    _navigation.send(NavigationEvent.PaymentError(error))
                },
            )
        }
    }

    override fun delete() {
        val paymentMethod = _state.value.selectedPaymentMethod ?: return
        logReporter.debug(
            "Deleting vaulted payment method: type=${paymentMethod.paymentMethodType}",
            component = TAG,
        )
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            deleteVaultedPaymentMethodUseCase(paymentMethod.id).fold(
                onSuccess = {
                    logReporter.info("Vaulted payment method deleted successfully", component = TAG)
                    val updatedMethods = _state.value.paymentMethods - paymentMethod
                    _state.update {
                        it.copy(
                            paymentMethods = updatedMethods,
                            selectedPaymentMethod = updatedMethods.firstOrNull(),
                            cvv = null,
                            isLoading = false,
                            error = null,
                        )
                    }
                    _navigation.send(NavigationEvent.DeleteSuccess)
                },
                onFailure = { error ->
                    logReporter.error(
                        "Failed to delete vaulted payment method: ${error.message}",
                        component = TAG,
                        throwable = error,
                    )
                    _state.update { it.copy(isLoading = false, error = error) }
                },
            )
        }
    }

    override fun toggleEditMode() {
        _state.update { it.copy(isEditMode = !it.isEditMode) }
    }

    companion object {
        private const val TAG = "VaultViewModel"
    }
}
