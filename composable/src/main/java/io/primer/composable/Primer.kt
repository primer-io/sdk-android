package io.primer.composable

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.data.settings.PrimerSettings
import io.primer.composable.internal.presentation.checkout.checkout
import io.primer.composable.scope.PrimerCardFormScope
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope
import io.primer.composable.scope.PrimerCheckoutScope

// TODO COMPOSABLE add kdocs
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
    fun showCheckout(
        modifier: Modifier = Modifier,
        container: (@Composable PrimerCheckoutScope.(content: @Composable () -> Unit) -> Unit)? = null,
        splashScreen: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
        loadingScreen: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
        paymentSelectionScreen: (@Composable PrimerPaymentMethodSelectionScope.() -> Unit)? = null,
        cardFormScreen: (@Composable PrimerCardFormScope.() -> Unit)? = null,
        successScreen: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
        errorScreen: (@Composable PrimerCheckoutScope.(cause: String) -> Unit)? = null,
    ) = checkout(
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
