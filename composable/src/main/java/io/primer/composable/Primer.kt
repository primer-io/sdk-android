package io.primer.composable

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.data.settings.PrimerSettings
import io.primer.composable.internal.presentation.checkout.Checkout
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
    fun ComposableCheckout(
        modifier: Modifier = Modifier,
        scope: (PrimerCheckoutScope) -> Unit
    ) = Checkout(modifier = modifier, scope = scope)
}
