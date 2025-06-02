package io.primer.composable.scope

import io.primer.composable.model.PrimerPaymentMethod
import kotlinx.coroutines.flow.StateFlow

interface PaymentMethodSelectionScope {

    val state: StateFlow<State>

    fun onPaymentMethodSelected(paymentMethod: PrimerPaymentMethod)

    sealed interface State {
        data object Loading : State
        data class Ready(val paymentMethods: List<PrimerPaymentMethod>) : State
        data class Error(val exception: Throwable) : State
    }

}
