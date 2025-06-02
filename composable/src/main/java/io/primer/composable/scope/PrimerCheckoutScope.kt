package io.primer.composable.scope

import io.primer.android.domain.error.models.PrimerError
import io.primer.composable.model.PrimerPaymentMethod
import kotlinx.coroutines.flow.StateFlow

interface PrimerCheckoutScope {

    val state: StateFlow<State>

    fun selectPaymentMethod(method: PrimerPaymentMethod)

    fun clearSelectedPaymentMethod()

    fun cleanup()

    sealed interface State {

        data object Loading : State

        data class Ready(val paymentMethods: List<PrimerPaymentMethod>) : State

        data class SelectedPaymentMethod(val paymentMethod: PrimerPaymentMethod) : State

        data class Error(val error: PrimerError) : State

        data object Success : State
    }
}
