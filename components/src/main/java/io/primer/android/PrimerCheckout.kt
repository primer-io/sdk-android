package io.primer.android

import android.R.attr.theme
import androidx.compose.runtime.Composable
import io.primer.android.core.ExperimentalPrimerApi
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
 * @param clientToken The client token obtained from your backend, containing checkout configuration
 * @param primerSettings SDK configuration settings including API environment, logging level, etc.
 * @param theme Custom theme configuration for visual appearance
 * @param scope Optional lambda providing access to [PrimerCheckoutScope] for customization and state monitoring
 */
@ExperimentalPrimerApi
@Composable
fun PrimerCheckout(
    clientToken: String,
    primerSettings: PrimerSettings = PrimerSettings(),
    primerTheme: PrimerTheme = PrimerTheme(),
    scope: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
) {
    Checkout(
        clientToken = clientToken,
        primerSettings = primerSettings,
        primerTheme = primerTheme,
        scope = scope,
    )
}
