package io.primer.composable.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodItem
import kotlinx.coroutines.flow.StateFlow

interface PaymentMethodSelectionScope {

    val state: StateFlow<State>

    fun onPaymentMethodSelected(paymentMethod: PrimerComposablePaymentMethod)

    sealed interface State {
        data object Loading : State
        data class Ready(
            val paymentMethods: List<PrimerComposablePaymentMethod>,
            val currency: java.util.Currency?
        ) : State
        data class Error(val exception: Throwable) : State
    }

    companion object {

        @Composable
        fun PaymentMethodSelectionScope.PrimerPaymentMethodItem(
            modifier: Modifier = Modifier,
            primerPaymentMethod: PrimerComposablePaymentMethod,
            currency: java.util.Currency? = null,
        ) = PaymentMethodItem(modifier, primerPaymentMethod, currency)
    }
}
