package io.primer.composable.internal.presentation.checkout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier,
    container: (@Composable PrimerCheckoutScope.(content: @Composable () -> Unit) -> Unit)?,
    splashScreen: (@Composable PrimerCheckoutScope.() -> Unit)?,
    errorScreen: (@Composable PrimerCheckoutScope.(cause: String) -> Unit)?,
    loadingScreen: (@Composable PrimerCheckoutScope.() -> Unit)?,
    paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit)?,
    cardFormScreen: (@Composable CardFormScope.() -> Unit)?,
    successScreen: (@Composable PrimerCheckoutScope.() -> Unit)?,
) = with(viewModel<CheckoutViewModel>()) {
    val context = LocalContext.current

    DisposableEffect(clientToken) {
        initialize(context, clientToken, primerSettings)
        onDispose { onDismiss() }
    }

    when (val state = state.collectAsStateWithLifecycle().value) {
        PrimerCheckoutScope.State.Dismissed -> Unit
        PrimerCheckoutScope.State.Initializing -> splashScreen?.invoke(this)
        is PrimerCheckoutScope.State.Error -> errorScreen?.invoke(this, "${state.exception.message}")
        PrimerCheckoutScope.State.Ready -> {
            val content: @Composable () -> Unit = {
                CheckoutNavHost(
                    modifier = modifier,
                    splashScreen = { splashScreen?.invoke(this@with) ?: SplashScreen() },
                    loadingScreen = { loadingScreen?.invoke(this@with) ?: LoadingScreen() },
                    paymentSelectionScreen = { paymentSelectionScreen?.invoke(this) ?: PaymentMethodSelectionScreen() },
                    cardFormScopeScreen = { cardFormScreen?.invoke(this) ?: CardFormScreen() },
                    successScreen = { successScreen?.invoke(this@with) ?: SuccessScreen() },
                    errorScreen = { errorScreen?.invoke(this@with, it) ?: ErrorScreen() },
                )
            }

            container?.invoke(this, content) ?: run {
                ModalBottomSheet(
                    onDismissRequest = ::onDismiss,
                    dragHandle = {},
                    modifier = modifier,
                    content = { content() },
                )
            }
        }
    }
}
