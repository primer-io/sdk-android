package io.primer.components

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.components.ui.checkout.Default
import io.primer.components.ui.checkout.PrimerCheckoutViewModel

/**
 * The main entry point for the Components SDK.
 *
 * - If [content] is provided, it is used to render a custom UI using [Primer.Scope.Checkout].
 * - If [content] is null, the default implementation is rendered.
 *
 * @param content An optional composable function allowing merchants to customize the UI.
 */
@Composable
fun PrimerCheckout(content: (@Composable Primer.Scope.Checkout.() -> Unit)? = null) {
    val checkoutViewModel: PrimerCheckoutViewModel = viewModel()
    content?.let { it(checkoutViewModel) } ?: Default(checkoutViewModel)
}
