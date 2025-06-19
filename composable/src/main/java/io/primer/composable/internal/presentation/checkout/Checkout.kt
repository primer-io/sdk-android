package io.primer.composable.internal.presentation.checkout

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.PrimerTheme
import io.primer.composable.scope.PrimerCardFormScope
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope
import io.primer.composable.scope.PrimerCheckoutScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Primer.checkout(
    modifier: Modifier = Modifier,
    container: (@Composable PrimerCheckoutScope.(content: @Composable () -> Unit) -> Unit)?,
    splashScreen: (@Composable PrimerCheckoutScope.() -> Unit)?,
    errorScreen: (@Composable PrimerCheckoutScope.(cause: String) -> Unit)?,
    loadingScreen: (@Composable PrimerCheckoutScope.() -> Unit)?,
    paymentSelectionScreen: (@Composable PrimerPaymentMethodSelectionScope.() -> Unit)?,
    cardFormScreen: (@Composable PrimerCardFormScope.() -> Unit)?,
    successScreen: (@Composable PrimerCheckoutScope.() -> Unit)?,
) : PrimerCheckoutScope = with(viewModel<CheckoutViewModel>()) {

    // TODO COMPOSABLE is this the correct place to initialise theme?
    PrimerTheme {
        val context = LocalContext.current

        DisposableEffect(clientToken) {
            initialize(context, clientToken, primerSettings)
            onDispose { onDismiss() }
        }

        when (val state = state.collectAsStateWithLifecycle().value) {
            PrimerCheckoutScope.State.Dismissed -> Unit
            // TODO COMPOSABLE why is this not shown?
            PrimerCheckoutScope.State.Initializing -> {
                splashScreen?.let { SplashScreen = it }
                SplashScreen()
            }
            is PrimerCheckoutScope.State.Error -> {
                errorScreen?.let { ErrorScreen = it }
                ErrorScreen("${state.exception.message}")
            }
            PrimerCheckoutScope.State.Ready -> {
                // Apply custom composables if provided
                container?.let { Container = it }
                splashScreen?.let { SplashScreen = it }
                loadingScreen?.let { LoadingScreen = it }
                successScreen?.let { SuccessScreen = it }
                errorScreen?.let { ErrorScreen = it }
                
                // Apply nested scope composables if provided
                paymentSelectionScreen?.let { screen -> 
                    paymentSelectionScope.PrimerPaymentSelectionScreen = { screen.invoke(paymentSelectionScope) }
                }
                cardFormScreen?.let { screen -> 
                    cardFormScope.PrimerCardFormScreen = { screen.invoke(cardFormScope) }
                }

                val content: @Composable () -> Unit = {
                    CheckoutNavHost(modifier = modifier)
                }

                Container.invoke(this, content)
            }
        }
    }

    return this
}
