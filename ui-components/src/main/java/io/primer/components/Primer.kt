package io.primer.components

import androidx.compose.runtime.Composable
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.components.clean.internal.presentation.checkout.Checkout

/**
 * Main entry point for Primer SDK integration.
 *
 * Provides a simplified API for configuring and displaying the Primer checkout experience
 * in Jetpack Compose applications.
 *
 * ## Usage Example
 * ```kotlin
 * // 1. Configure Primer with your client token
 * Primer.configure(
 *     clientToken = "your-client-token-here",
 *     settings = PrimerSettings(
 *         // your settings configuration
 *     )
 * )
 *
 * // 2. Display the checkout UI
 * Primer.ComposableCheckout(
 *     successContent = {
 *         // Show success UI
 *     },
 *     failureContent = { error ->
 *         // Handle error
 *     }
 * )
 * ```
 */
object Primer {

    private var clientToken: String? = null
    private var settings: PrimerSettings? = null

    /**
     * Configures the Primer SDK with required credentials and optional settings.
     *
     * Must be called before using [ComposableCheckout]. Typically called during
     * app initialization or before presenting the checkout flow.
     *
     * @param clientToken The client token obtained from your backend after creating a client session.
     *                    This token authorizes the SDK to process payments for the current session.
     * @param settings Optional configuration for customizing the checkout experience, including
     *                 payment methods, UI customization, and behavioral settings.
     *
     * @throws IllegalStateException if called with a null or empty client token
     */
    fun configure(
        clientToken: String,
        settings: PrimerSettings? = null,
    ) {
        this.clientToken = clientToken
        this.settings = settings
    }

    /**
     * Displays the Primer checkout UI as a Composable.
     *
     * This is the main UI component for presenting the payment flow to users.
     * The checkout handles the entire payment process including payment method selection,
     * form validation, and payment processing.
     *
     * @param successContent Optional composable to display when payment completes successfully.
     *                       If not provided, the checkout will handle its own success state.
     * @param failureContent Optional composable to display when payment fails.
     *                       Receives the [PrimerError] that caused the failure.
     *                       If not provided, the checkout will handle its own error state.
     * @param content Optional composable lambda that receives a [PrimerCheckout] scope,
     *               allowing for custom UI elements within the checkout flow.
     *
     * @throws IllegalStateException if [configure] has not been called with valid parameters
     *
     * @see configure
     * @see PrimerSettings
     * @see PrimerError
     */
    @Composable
    fun ComposableCheckout(
        successContent: (@Composable () -> Unit)? = null,
        failureContent: (@Composable (cause: PrimerError) -> Unit)? = null,
        content: (@Composable PrimerCheckout.() -> Unit)? = null,
    ) {
        Checkout(
            clientToken = requireNotNull(clientToken) { "Client token is required" },
            primerSettings = requireNotNull(settings) { "Primer settings are required" },
            successContent = successContent,
            failureContent = failureContent,
            content = content
        )
    }

}
