package io.primer.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.data.settings.PrimerSettings
import io.primer.composable.internal.presentation.checkout.Checkout
import io.primer.composable.scope.PrimerCheckoutScope

// TODO COMPOSABLE add kdocs
@Composable
fun PrimerCheckout(
    modifier: Modifier = Modifier,
    clientToken: String,
    settings: PrimerSettings = PrimerSettings(),
    scope: ((PrimerCheckoutScope) -> Unit)? = null,
) {
    Checkout(
        modifier = modifier,
        clientToken = clientToken,
        settings = settings,
        scope = scope
    )
}
