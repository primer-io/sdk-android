package io.primer.composable

import androidx.compose.runtime.Composable
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.composable.internal.presentation.checkout.Checkout
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope

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
        errorScreen: (@Composable (cause: PrimerError) -> Unit)? = null
    ) {
        Checkout(
            clientToken = clientToken,
            primerSettings = primerSettings,
            loadingScreen = loadingScreen,
            paymentSelectionScreen = paymentSelectionScreen,
            cardFormScopeScreen = cardFormScopeScreen,
            successScreen = successScreen,
            errorScreen = errorScreen
        )
    }

}
