package io.primer.composable.internal.presentation.checkout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.composable.Primer
import io.primer.composable.internal.presentation.screens.card.CardFormScreen
import io.primer.composable.internal.presentation.screens.error.ErrorScreen
import io.primer.composable.internal.presentation.screens.loading.LoadingScreen
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionScreen
import io.primer.composable.internal.presentation.screens.splash.SplashScreen
import io.primer.composable.internal.presentation.screens.success.SuccessScreen
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope
import io.primer.composable.scope.PrimerCheckoutScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Primer.Checkout(
    splashScreen: (@Composable PrimerCheckoutScope.() -> Unit)? = { SplashScreen() },
    errorScreen: (@Composable PrimerCheckoutScope.(cause: String) -> Unit)? = { ErrorScreen() },
    loadingScreen: (@Composable PrimerCheckoutScope.() -> Unit)? = { LoadingScreen() },
    paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit)? = { PaymentMethodSelectionScreen() },
    cardFormScreen: (@Composable CardFormScope.() -> Unit)? = { CardFormScreen() },
    successScreen: (@Composable PrimerCheckoutScope.() -> Unit)? = { SuccessScreen() },
) = with(viewModel<CheckoutViewModel>()) {

    val context = LocalContext.current

    DisposableEffect(clientToken) {
        initialize(context, clientToken, primerSettings)
        onDispose { cleanup() }
    }

    when (val state = state.collectAsStateWithLifecycle().value) {
        PrimerCheckoutScope.State.NotInitialized -> Unit
        PrimerCheckoutScope.State.Initializing -> splashScreen?.invoke(this)
        is PrimerCheckoutScope.State.Error -> errorScreen?.invoke(this, "${state.exception.message}")
        PrimerCheckoutScope.State.Ready -> {
            ModalBottomSheet(onDismissRequest = ::cleanup) {
                CheckoutNavHost(
                    loadingScreen = { loadingScreen?.invoke(this@with) },
                    paymentSelectionScreen = { paymentSelectionScreen?.invoke(this) },
                    cardFormScopeScreen = { cardFormScreen?.invoke(this) },
                    successScreen = { successScreen?.invoke(this@with) },
                    errorScreen = { errorScreen?.invoke(this@with, it) },
                )
            }
        }
    }
}
