package io.primer.android.internal.presentation.screens.nativeUi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.models.PrimerUnknownError
import io.primer.android.internal.domain.Cleanable
import io.primer.android.internal.domain.error.PrimerErrorException
import io.primer.android.internal.domain.usecase.StartNativeUiPaymentUseCase
import io.primer.android.scope.PrimerNativeUiPaymentMethodScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class NativeUiPaymentMethodViewModel(
    private val paymentMethodType: String,
    private val startNativeUiPaymentUseCase: StartNativeUiPaymentUseCase,
    private val logReporter: LogReporter,
) : ViewModel(), PrimerNativeUiPaymentMethodScope, Cleanable {

    sealed interface NavigationEvent {
        data class PaymentSuccess(val checkoutData: PrimerCheckoutData) : NavigationEvent
        data class PaymentError(val error: PrimerError) : NavigationEvent
    }

    private val _state = MutableStateFlow(PrimerNativeUiPaymentMethodScope.State())
    override val state: StateFlow<PrimerNativeUiPaymentMethodScope.State> = _state.asStateFlow()

    private val _navigation = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()

    init {
        logReporter.info("Native UI payment initiated: type=$paymentMethodType", component = TAG)
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true)

            startNativeUiPaymentUseCase(paymentMethodType).fold(
                onSuccess = { checkoutData ->
                    logReporter.info(
                        "Native UI payment completed successfully: type=$paymentMethodType",
                        component = TAG,
                    )
                    _state.value = _state.value.copy(isProcessing = false)
                    _navigation.trySend(NavigationEvent.PaymentSuccess(checkoutData))
                },
                onFailure = { error ->
                    logReporter.error(
                        "Native UI payment failed: type=$paymentMethodType, error=${error.message}",
                        component = TAG,
                        throwable = error,
                    )
                    val primerError = (error as? PrimerErrorException)?.primerError
                        ?: PrimerUnknownError(error.message ?: "Payment failed")
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        error = primerError.description,
                    )
                    _navigation.trySend(NavigationEvent.PaymentError(primerError))
                },
            )
        }
    }

    override fun cleanup() {
        startNativeUiPaymentUseCase.cleanup()
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    companion object {
        private const val TAG = "NativeUiPaymentMethodViewModel"
    }
}
