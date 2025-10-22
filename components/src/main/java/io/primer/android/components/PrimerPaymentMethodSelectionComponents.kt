package io.primer.android.components

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.presentation.screens.paymentMethodSelection.DefaultPaymentMethodSelectionScreen
import io.primer.android.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionViewModel
import io.primer.android.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionViewModelFactory
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list.DefaultPaymentMethodItem
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

class PrimerPaymentMethodSelectionComponents : DISdkComponent {

    /**
     * Access to vaulted payment method components for customization.
     */
    val vaultedComponents: PrimerVaultedComponents
        get() = resolve()

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
     * Composable function for rendering a single payment method option.
     *
     * @param paymentMethodItem The payment method to display
     */
    var paymentMethodItem:
        @Composable PrimerPaymentMethodSelectionScope.(paymentMethod: PrimerComposablePaymentMethod) -> Unit =
        { paymentMethod ->
            DefaultPaymentMethodItem(paymentMethod)
        }
}
