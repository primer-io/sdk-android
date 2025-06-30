package io.primer.android.internal.presentation.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import io.primer.android.internal.presentation.screens.paymentMethodSelection.DefaultPaymentMethodSelectionScreen
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list.PaymentMethodItemCard
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

internal abstract class DefaultPaymentMethodSelectionScope : ViewModel(), PrimerPaymentMethodSelectionScope {

    override var screen: @Composable () -> Unit = {
        DefaultPaymentMethodSelectionScreen()
    }

    override var paymentMethodCard: @Composable (modifier: Modifier) -> Unit =
        { modifier -> PaymentMethodItemCard(modifier = modifier) }
}
