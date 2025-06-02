package io.primer.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.data.settings.PrimerSettings
import io.primer.composable.internal.presentation.checkout.CheckoutNavHost
import io.primer.composable.internal.presentation.checkout.CheckoutViewModel
import io.primer.composable.internal.presentation.screens.card.CardFormScreen
import io.primer.composable.internal.presentation.screens.error.ErrorScreen
import io.primer.composable.internal.presentation.screens.loading.LoadingScreen
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionScreen
import io.primer.composable.internal.presentation.screens.success.SuccessScreen
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope
import io.primer.composable.scope.PrimerCheckoutScope

// TODO Overridable Composable functions with default values are not currently supported
//interface Primer {
//
//    fun configure(
//        clientToken: String,
//        settings: PrimerSettings = PrimerSettings(),
//    )
//
//    @Composable
//    fun ComposableCheckout(
//        loadingScreen: (@Composable () -> Unit)? = null,
//        paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit)? = null,
//        cardFormScopeScreen: (@Composable CardFormScope.() -> Unit)? = null,
//        successScreen: (@Composable () -> Unit)? = null,
//        errorScreen: (@Composable (cause: PrimerError) -> Unit)? = null
//    )
//
//    companion object {
//        val instance: Primer = PrimerImpl()
//    }
//
//}

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
        cardFormScreen: (@Composable CardFormScope.() -> Unit)? = null,
        successScreen: (@Composable () -> Unit)? = null,
        errorScreen: (@Composable (cause: String) -> Unit)? = null
    ) {
        val checkoutViewModel = viewModel<CheckoutViewModel>()
        val context = LocalContext.current
        
        DisposableEffect(clientToken) {
            checkoutViewModel.initialize(context, clientToken, primerSettings)
            onDispose { checkoutViewModel.cleanup() }
        }

        when (val state = checkoutViewModel.state.collectAsStateWithLifecycle().value) {
            PrimerCheckoutScope.State.NotInitialized -> Unit
            PrimerCheckoutScope.State.Initializing -> {
                loadingScreen?.invoke() ?: LoadingScreen(text = "Splash loading")
            }
            is PrimerCheckoutScope.State.Error ->
                errorScreen?.invoke(state.exception.message!!) ?: ErrorScreen(message = state.exception.message!!)
            PrimerCheckoutScope.State.Ready -> {
//                    navHostController.navigate(Screen.PaymentsList.route)
                CheckoutNavHost(
                    loadingScreen = { loadingScreen?.invoke() ?: LoadingScreen() },
                    paymentSelectionScreen = { paymentSelectionScreen?.let { it() } ?: PaymentMethodSelectionScreen() },
                    cardFormScopeScreen = { cardFormScreen?.let { it() } ?: CardFormScreen() },
                    successScreen = { successScreen?.invoke() ?: SuccessScreen(message = "Success!") },
                    errorScreen = { error -> errorScreen?.let { it(error) } ?: ErrorScreen(message = error) }
                )
            }
        }
    }
}
