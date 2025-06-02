package io.primer.composable.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.composable.Primer
import io.primer.composable.internal.presentation.checkout.CheckoutViewModel
import io.primer.composable.internal.presentation.checkout.NavigationHost
import io.primer.composable.internal.presentation.screens.card.CardFormScreen
import io.primer.composable.internal.presentation.screens.error.ErrorScreen
import io.primer.composable.internal.presentation.screens.loading.LoadingScreen
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionScreen
import io.primer.composable.internal.presentation.screens.success.SuccessScreen
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope
import io.primer.composable.scope.PrimerCheckoutScope

internal object PrimerImpl: Primer {

    private lateinit var clientToken: String
    private lateinit var primerSettings: PrimerSettings

    override fun configure(
        clientToken: String,
        settings: PrimerSettings,
    ) {
        this.clientToken = clientToken
        this.primerSettings = settings
    }

    @Composable
    override fun ComposableCheckout(
        loadingScreen: (@Composable () -> Unit)?,
        paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit)?,
        cardFormScopeScreen: (@Composable CardFormScope.() -> Unit)?,
        successScreen: (@Composable () -> Unit)?,
        errorScreen: (@Composable (cause: PrimerError) -> Unit)?
    ) {

        val checkoutViewModel = viewModel<CheckoutViewModel>()
        val state by checkoutViewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current

        DisposableEffect(clientToken) {
            checkoutViewModel.initialize(context, clientToken, primerSettings)
            onDispose { checkoutViewModel.cleanup() }
        }

        when (state) {

            is PrimerCheckoutScope.State.Error,
            PrimerCheckoutScope.State.Initializing,
            PrimerCheckoutScope.State.NotInitialized -> Unit

            PrimerCheckoutScope.State.Ready -> {
                NavigationHost(
                    loadingScreen = loadingScreen ?: { LoadingScreen() },
                    paymentSelectionScreen = paymentSelectionScreen ?: { PaymentMethodSelectionScreen() },
                    cardFormScopeScreen = cardFormScopeScreen ?: { CardFormScreen() },
                    successScreen = successScreen ?: { SuccessScreen(message = "Success!") },
                    errorScreen = errorScreen ?: { ErrorScreen(message = it.description) }
                )
            }
        }
    }

}
