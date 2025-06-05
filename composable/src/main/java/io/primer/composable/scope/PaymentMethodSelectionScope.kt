package io.primer.composable.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodItem
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionScreen
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

    companion object {

        @Composable
        fun PaymentMethodSelectionScope.PrimerPaymentMethodSelectionScreen(
            modifier: Modifier = Modifier,
            content: (@Composable PaymentMethodSelectionScope.() -> Unit)? = null
        ) = content?.invoke(this) ?: PaymentMethodSelectionScreen(modifier)

        @Composable
        fun PaymentMethodSelectionScope.PrimerPaymentMethodItem(
            modifier: Modifier = Modifier,
            primerPaymentMethod: PrimerPaymentMethod,
        ) = PaymentMethodItem(modifier, primerPaymentMethod)

    }
}
