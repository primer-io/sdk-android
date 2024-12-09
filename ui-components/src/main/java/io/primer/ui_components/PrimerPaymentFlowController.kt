package io.primer.ui_components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class PaymentStateX {
    data object Idle : PaymentStateX()
    data object Loading : PaymentStateX()
    data object Success : PaymentStateX()
    data object AwaitUserInput : PaymentStateX()
    data class SelectedPaymentMethod(val method: PrimerHeadlessUniversalCheckoutPaymentMethod) : PaymentStateX()
    data class Error(val message: String) : PaymentStateX()
}

class PrimerPaymentFlowController internal constructor() : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentStateX?>(null)
    val paymentState: StateFlow<PaymentStateX?> = _paymentState.asStateFlow()

    fun setSelectedPaymentMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod) {
        updatePaymentState(PaymentStateX.SelectedPaymentMethod(method))
    }

    fun startPaymentFlow() {}

    internal fun updatePaymentState(stateX: PaymentStateX) {
        _paymentState.update { stateX }
    }

    companion object {

        fun provideInstance(
            owner: ViewModelStoreOwner
        ): PrimerPaymentFlowController {
            return ViewModelProvider(
                owner,
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(
                        modelClass: Class<T>,
                        extras: CreationExtras
                    ): T {
                        return PrimerPaymentFlowController() as T
                    }
                }
            ).get(
                key = PrimerPaymentFlowController::class.java.canonicalName.orEmpty(),
                modelClass = PrimerPaymentFlowController::class.java
            )
        }
    }
}