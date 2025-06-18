package io.primer.composable.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.components.PaymentMethodItemCard
import kotlinx.coroutines.flow.StateFlow

interface PaymentMethodSelectionScope {

    val state: StateFlow<State>

    fun onPaymentMethodSelected(paymentMethod: PrimerComposablePaymentMethod)

    fun onCancel()

    sealed interface State {
        data object Loading : State
        data class Ready(
            val paymentMethods: List<PrimerComposablePaymentMethod>,
            val title: String,
        ) : State

        data class Error(val exception: Throwable) : State
    }

    companion object {

        @Composable
        fun PaymentMethodSelectionScope.PrimerPaymentMethodCard(
            modifier: Modifier = Modifier,
            onPaymentMethodSelected: () -> Unit,
        ) = PaymentMethodItemCard(modifier = modifier, onPaymentMethodSelected = onPaymentMethodSelected)
    }
}
