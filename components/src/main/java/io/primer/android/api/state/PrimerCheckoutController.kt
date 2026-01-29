package io.primer.android.api.state

import androidx.compose.runtime.Stable
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Root checkout session coordinator.
 *
 * Create via [rememberPrimerCheckoutController]. This provides:
 * - Session state observation via [state]
 * - Session refresh via [refresh]
 *
 * For payment method interactions, use the specialized state holders:
 * - [io.primer.android.api.components.paymentMethods.rememberPaymentMethodsController] - available payment methods
 * - [io.primer.android.api.components.paymentMethods.rememberVaultedPaymentMethodsController] - saved payment methods
 * - [io.primer.android.api.components.card.rememberCardFormController] - card form state and actions
 *
 * ## Example: Observing session state
 * ```kotlin
 * val checkout = rememberPrimerCheckoutState(clientToken) { result ->
 *     when (result) {
 *         is PrimerResult.Success -> navigateToConfirmation(result.paymentId)
 *         is PrimerResult.Failure -> showError(result.error)
 *         is PrimerResult.Cancelled -> { /* user cancelled */ }
 *         is PrimerResult.TokenCreated -> { /* MANUAL flow */ }
 *     }
 * }
 *
 * val state by checkout.state.collectAsState()
 * if (state.isLoading) {
 *     CircularProgressIndicator()
 * } else {
 *     Text("Total: ${checkout.formatAmount(state.clientSession?.totalAmount ?: 0)}")
 * }
 * ```
 */
@Stable
interface PrimerCheckoutController {
    /** Combined checkout session state */
    val state: StateFlow<PrimerCheckoutState>

    /**
     * Refresh client session by reinitializing the SDK.
     *
     * Call this to refresh payment methods and session data,
     * for example after updating the order on your server.
     *
     * ## Example:
     * ```kotlin
     * // After updating order on server
     * checkout.refresh()
     * ```
     */
    fun refresh()
}

/**
 * Format an amount in minor units (cents) to a currency string.
 *
 * Uses the currency from client session for proper formatting.
 *
 * ## Example:
 * ```kotlin
 * val checkout = rememberPrimerCheckoutState(clientToken) { result -> }
 * val state by checkout.state.collectAsState()
 *
 * Text("Total: ${checkout.formatAmount(state.clientSession?.order?.totalAmount ?: 0)}")
 * ```
 *
 * @param amountInCents Amount in minor units (e.g., cents)
 * @return Formatted currency string (e.g., "$99.99")
 */
fun PrimerCheckoutController.formatAmount(amountInCents: Int): String {
    return (this as CheckoutViewModel).formatAmount(amountInCents)
}
