package io.primer.composable.internal.presentation.scope

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.composable.internal.presentation.checkout.CheckoutNavigator
import io.primer.composable.internal.presentation.screens.error.DefaultErrorScreen
import io.primer.composable.internal.presentation.screens.loading.DefaultLoadingScreen
import io.primer.composable.internal.presentation.screens.splash.DefaultSplashScreen
import io.primer.composable.internal.presentation.screens.success.DefaultSuccessScreen
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.scope.PrimerCardFormScope
import io.primer.composable.scope.PrimerCheckoutScope
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope
import kotlinx.coroutines.launch

internal abstract class DefaultCheckoutScope : ViewModel(), PrimerCheckoutScope, DISdkComponent {

    private val checkoutNavigator: CheckoutNavigator by lazy { resolve() }

    @OptIn(ExperimentalMaterial3Api::class)
    override var container: @Composable (content: @Composable () -> Unit) -> Unit = { content ->
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
            onDismissRequest = ::onDismiss,
            dragHandle = {},
            containerColor = LocalPrimerColorTokens.current.primerColorBackground,
            content = { content() },
        )
    }

    override var splashScreen: @Composable () -> Unit = {
        DefaultSplashScreen()
    }

    override var loadingScreen: @Composable () -> Unit = {
        DefaultLoadingScreen()
    }

    override var successScreen: @Composable () -> Unit = {
        DefaultSuccessScreen()
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
            }
        )
    }

    override val cardForm: PrimerCardFormScope by lazy { resolve() }

    override val paymentMethodSelection: PrimerPaymentMethodSelectionScope by lazy { resolve() }
}
