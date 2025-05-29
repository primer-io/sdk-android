package io.primer.components

import io.primer.components.clean.model.PrimerPaymentMethod
import kotlinx.coroutines.flow.StateFlow

interface Primer {

    val state: StateFlow<State>

    fun selectPaymentMethod(method: PrimerPaymentMethod)

    fun clearSelectedPaymentMethod()

    sealed interface State {

        data object Loading : State

        data class Ready(val paymentMethods: List<PrimerPaymentMethod>) : State

        data class Selected(val paymentMethod: PrimerPaymentMethod) : State

        data class Error(val message: String) : State
    }
}
