package io.primer.android.internal.presentation.scope

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.components.CheckoutBottomSheet
import io.primer.android.internal.presentation.checkout.components.DefaultErrorScreen
import io.primer.android.internal.presentation.checkout.components.DefaultLoadingScreen
import io.primer.android.internal.presentation.checkout.components.DefaultSplashScreen
import io.primer.android.internal.presentation.checkout.components.DefaultSuccessScreen
import io.primer.android.scope.PrimerCardFormScope
import io.primer.android.scope.PrimerCheckoutScope
import io.primer.android.scope.PrimerPaymentMethodSelectionScope
import kotlinx.coroutines.launch

internal abstract class DefaultCheckoutScope : ViewModel(), PrimerCheckoutScope, DISdkComponent {

    private val checkoutNavigator: CheckoutNavigator by lazy { resolve() }
    override val cardForm: PrimerCardFormScope by lazy { resolve() }
    override val paymentMethodSelection: PrimerPaymentMethodSelectionScope by lazy { resolve() }

    override var container: @Composable (content: @Composable () -> Unit) -> Unit = { content ->
        CheckoutBottomSheet(
            onDismiss = ::onDismiss,
            content = content
        )
    }

    override var splashScreen: @Composable () -> Unit = {
        DefaultSplashScreen()
    }

    override var loadingScreen: @Composable () -> Unit = {
        DefaultLoadingScreen()
    }

    override var successScreen: @Composable () -> Unit = {
        DefaultSuccessScreen(
            onDismiss = {
                viewModelScope.launch {
                    checkoutNavigator.dismiss()
                }
            },
        )
    }

    override var errorScreen: @Composable (message: String) -> Unit = { message ->
        DefaultErrorScreen(
            title = "Payment failed",
            message = message,
            onRetryClick = {
                viewModelScope.launch {
                    checkoutNavigator.navigateBack()
                }
            },
            onOtherPaymentMethodClick = {
                viewModelScope.launch {
                    checkoutNavigator.navigateToPaymentMethodsList()
                }
            },
        )
    }
}
