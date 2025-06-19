package io.primer.composable.internal.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.DefaultPaymentMethodSelectionScreen
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.components.PaymentMethodItemCard
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope

internal abstract class PaymentMethodSelectionScopeDefaults : PrimerPaymentMethodSelectionScope {
    
    // Default composable implementations
    override var PrimerPaymentSelectionScreen: @Composable () -> Unit = {
        DefaultPaymentMethodSelectionScreen()
    }
    
    override var PrimerPaymentMethodCard: @Composable (modifier: Modifier, onPaymentMethodSelected: () -> Unit) -> Unit = { modifier, onSelected ->
        PaymentMethodItemCard(modifier = modifier, onPaymentMethodSelected = onSelected)
    }
}