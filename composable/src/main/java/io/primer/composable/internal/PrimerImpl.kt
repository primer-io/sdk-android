package io.primer.composable.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.primer.android.data.settings.PrimerSettings
import io.primer.composable.internal.presentation.checkout.CheckoutViewModel
import io.primer.composable.internal.presentation.checkout.NavigationHost
import io.primer.composable.internal.presentation.checkout.Screen
import io.primer.composable.internal.presentation.screens.card.CardFormScreen
import io.primer.composable.internal.presentation.screens.error.ErrorScreen
import io.primer.composable.internal.presentation.screens.loading.LoadingScreen
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionScreen
import io.primer.composable.internal.presentation.screens.success.SuccessScreen
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope
import io.primer.composable.scope.PrimerCheckoutScope

object Primer {

    private lateinit var clientToken: String
    private lateinit var primerSettings: PrimerSettings

    fun configure(
        clientToken: String,
        settings: PrimerSettings = PrimerSettings(),
    ) {
        this.clientToken = clientToken
        this.primerSettings = settings
    }

    @Composable
    fun ComposableCheckout(
        loadingScreen: (@Composable () -> Unit)? = null,
        paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit)? = null,
        cardFormScopeScreen: (@Composable CardFormScope.() -> Unit)? = null,
        successScreen: (@Composable () -> Unit)? = null,
        errorScreen: (@Composable (cause: String) -> Unit)? = null
    ) {
        val navHostController = rememberNavController()

        CompositionLocalProvider(LocalPrimerNavController provides navHostController) {
            val checkoutViewModel = viewModel<CheckoutViewModel>()
            val context = LocalContext.current
            DisposableEffect(clientToken) {
                checkoutViewModel.initialize(context, clientToken, primerSettings)
                onDispose { checkoutViewModel.cleanup() }
            }

            when (val state = checkoutViewModel.state.collectAsStateWithLifecycle().value) {
                PrimerCheckoutScope.State.NotInitialized -> Unit
                PrimerCheckoutScope.State.Initializing -> Unit
                is PrimerCheckoutScope.State.Error ->
                    errorScreen?.invoke(state.exception.message!!) ?: ErrorScreen(message = state.exception.message!!)
                PrimerCheckoutScope.State.Ready -> navHostController.navigate(Screen.PaymentsList)
            }

            NavigationHost(
                loadingScreen = { loadingScreen?.invoke() ?: LoadingScreen() },
                paymentSelectionScreen = { paymentSelectionScreen?.let { it() } ?: PaymentMethodSelectionScreen() },
                cardFormScopeScreen = { cardFormScopeScreen?.let { it() } ?: CardFormScreen() },
                successScreen = { successScreen?.invoke() ?: SuccessScreen(message = "Success!") },
                errorScreen = { error -> errorScreen?.let { it(error) } ?: ErrorScreen(message = error) }
            )
        }

    }

}

internal val LocalPrimerNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided")
}
