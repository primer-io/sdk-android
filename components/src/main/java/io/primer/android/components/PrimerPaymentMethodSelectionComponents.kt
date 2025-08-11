package io.primer.android.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.presentation.screens.paymentMethodSelection.DefaultPaymentMethodSelectionScreen
import io.primer.android.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionViewModel
import io.primer.android.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionViewModelFactory
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list.PaymentMethodItemCard
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

class PrimerPaymentMethodSelectionComponents : DISdkComponent {

    @Composable
    fun Screen() {
        screen(viewModel<PaymentMethodSelectionViewModel>(factory = resolve<PaymentMethodSelectionViewModelFactory>()))
    }

    /**
     * Composable function for the entire payment method selection screen layout.
     */
    var screen: @Composable PrimerPaymentMethodSelectionScope.() -> Unit = {
        DefaultPaymentMethodSelectionScreen()
    }

    /**
     * Composable function for the card button.
     *
     * @param modifier Modifier for styling the payment method card
     */
    var paymentMethodCard: @Composable PrimerPaymentMethodSelectionScope.(modifier: Modifier) -> Unit = {
        PaymentMethodItemCard(it)
    }
}
