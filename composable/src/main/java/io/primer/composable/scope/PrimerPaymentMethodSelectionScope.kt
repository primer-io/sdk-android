package io.primer.composable.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.domain.models.PrimerComposablePaymentMethod
import kotlinx.coroutines.flow.StateFlow

interface PrimerPaymentMethodSelectionScope {

    val state: StateFlow<State>

    fun onPaymentMethodSelected(paymentMethod: String)

    fun onCancel()

    sealed interface State {
        data object Loading : State
        data class Ready(
            val paymentMethods: List<PrimerComposablePaymentMethod>,
            val title: String,
        ) : State

        data class Error(val exception: Throwable) : State
    }

    var screen: @Composable () -> Unit
    var paymentMethodCard: @Composable (modifier: Modifier, onPaymentMethodSelected: (String) -> Unit) -> Unit
}
