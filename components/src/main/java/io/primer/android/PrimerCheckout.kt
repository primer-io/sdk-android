package io.primer.android

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.internal.presentation.checkout.Checkout
import io.primer.android.scope.PrimerCheckoutScope

/**
 * Entry point composable for Primer Checkout flow.
 *
 * Provides a complete checkout experience with payment method selection, form handling,
 * and transaction processing. The checkout flow is customizable through the [scope] parameter
 * which provides access to internal state and UI customization options.
 *
 * Example usage:
 * ```kotlin
 * PrimerCheckout(
 *     clientToken = "your-client-token",
 *     settings = PrimerSettings(
 *         apiEnvironment = PrimerApiEnvironment.SANDBOX
 *     ),
 *     scope = { checkoutScope ->
 *         // Customize container appearance
 *         checkoutScope.container = { content ->
 *             Card(modifier = Modifier.padding(16.dp)) {
 *                 content()
 *             }
 *         }
 *
 *         // Monitor checkout state
 *         LaunchedEffect(Unit) {
 *             checkoutScope.state.collect { state ->
 *                 when (state) {
 *                     is PrimerCheckoutScope.State.Ready -> {
 *                         // Checkout is ready for user interaction
 *                     }
 *                     is PrimerCheckoutScope.State.Error -> {
 *                         // Handle error
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * )
 * ```
 *
 * @param modifier Modifier to apply to the checkout container
 * @param clientToken The client token obtained from your backend, containing checkout configuration
 * @param settings SDK configuration settings including API environment, logging level, etc.
 * @param scope Optional lambda providing access to [PrimerCheckoutScope] for customization and state monitoring
 */
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
        scope = scope,
    )
}
