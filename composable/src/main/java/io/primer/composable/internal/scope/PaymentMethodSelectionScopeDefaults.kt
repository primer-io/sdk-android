package io.primer.composable.internal.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.DefaultPaymentMethodSelectionScreen
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.components.PaymentMethodItemCard
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

internal abstract class PaymentMethodSelectionScopeDefaults : ViewModel(), PrimerPaymentMethodSelectionScope {

    override var screen: @Composable () -> Unit = {
        DefaultPaymentMethodSelectionScreen()
    }

    override var paymentMethodCard: @Composable (modifier: Modifier, onPaymentMethodSelected: () -> Unit) -> Unit =
        { modifier, onSelected ->
            PaymentMethodItemCard(modifier = modifier, onPaymentMethodSelected = onSelected)
        }
}
