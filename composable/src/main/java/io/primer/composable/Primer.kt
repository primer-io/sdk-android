package io.primer.composable

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.data.settings.PrimerSettings
import io.primer.composable.internal.presentation.checkout.Checkout
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope
import io.primer.composable.scope.PrimerCheckoutScope

object Primer {

    internal lateinit var clientToken: String
    internal lateinit var primerSettings: PrimerSettings

    fun configure(
        clientToken: String,
        settings: PrimerSettings = PrimerSettings(),
    ) {
        this.clientToken = clientToken
        this.primerSettings = settings
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ComposableCheckout(
        modifier: Modifier = Modifier,
        container: (@Composable PrimerCheckoutScope.(content: @Composable () -> Unit) -> Unit)? = null,
        splashScreen: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
        loadingScreen: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
        paymentSelectionScreen: (@Composable PaymentMethodSelectionScope.() -> Unit)? = null,
        cardFormScreen: (@Composable CardFormScope.() -> Unit)? = null,
        successScreen: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
        errorScreen: (@Composable PrimerCheckoutScope.(cause: String) -> Unit)? = null,
    ) {
        Checkout(
            modifier = modifier,
            container = container,
            splashScreen = splashScreen,
            loadingScreen = loadingScreen,
            successScreen = successScreen,
            errorScreen = errorScreen,
            paymentSelectionScreen = paymentSelectionScreen,
            cardFormScreen = cardFormScreen,
        )
    }
}
