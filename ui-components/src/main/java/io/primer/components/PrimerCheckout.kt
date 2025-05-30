package io.primer.components

import io.primer.components.clean.model.PrimerPaymentMethod
import kotlinx.coroutines.flow.StateFlow

interface PrimerCheckout {

    val state: StateFlow<State>

    fun selectPaymentMethod(method: PrimerPaymentMethod)

    fun clearSelectedPaymentMethod()

    fun cleanup()

    sealed interface State {

        data object Loading : State

        data class Ready(val paymentMethods: List<PrimerPaymentMethod>) : State

        data class Error(val message: String) : State
    }
}
