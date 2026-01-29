package io.primer.android.api.state

import androidx.compose.runtime.Immutable
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError

/**
 * Checkout session state representing the entire checkout lifecycle.
 *
 * This sealed interface unifies all checkout states including loading, ready, and terminal
 * states (success, failure, cancelled). Observe state changes to react to checkout progress:
 *
 * ```kotlin
 * val checkout = rememberPrimerCheckoutController(clientToken, settings)
 * val state by checkout.state.collectAsState()
 *
 * when (state) {
 *     is PrimerCheckoutState.Loading -> CircularProgressIndicator()
 *     is PrimerCheckoutState.Ready -> ShowPaymentMethods(state.clientSession)
 *     is PrimerCheckoutState.Success -> navigateToConfirmation(state.checkoutData)
 *     is PrimerCheckoutState.Failure -> showError(state.error)
 *     is PrimerCheckoutState.Cancelled -> navigateBack()
 *     is PrimerCheckoutState.TokenCreated -> sendToServer(state.token)
 * }
 * ```
 */
@Immutable
sealed interface PrimerCheckoutState {
    /**
     * Checkout is initializing or refreshing.
     *
     * Display a loading indicator while the SDK initializes and fetches configuration.
     */
    data object Loading : PrimerCheckoutState

    /**
     * Checkout ready with session data loaded.
     *
     * The checkout is initialized and ready for payment. Display payment methods
     * and allow the user to proceed.
     *
     * @property clientSession Client session data (amount, currency, customer info, payment methods)
     */
    @Immutable
    data class Ready(
        val clientSession: PrimerClientSession,
    ) : PrimerCheckoutState

    /**
     * Payment completed successfully (AUTO flow).
     *
     * The payment was processed and completed. Navigate to confirmation screen
     * or complete the checkout flow.
     *
     * @property checkoutData Complete checkout data with payment ID, order ID, and additional info
     */
    @Immutable
    data class Success(
        val checkoutData: PrimerCheckoutData,
    ) : PrimerCheckoutState

    /**
     * Payment failed.
     *
     * The payment failed or an error occurred during checkout. Display the error
     * to the user and provide retry options.
     *
     * Contains a [PrimerError] with full error details for programmatic handling:
     * - [PrimerError.errorId] - Unique error identifier
     * - [PrimerError.description] - Human-readable message
     * - [PrimerError.diagnosticsId] - Reference ID for support
     * - [PrimerError.errorCode] - Error code (e.g., "card_declined")
     * - [PrimerError.recoverySuggestion] - Suggested action
     *
     * @property error The Primer error with full details
     */
    @Immutable
    data class Failure(
        val error: PrimerError,
    ) : PrimerCheckoutState

    /**
     * User cancelled the payment flow.
     *
     * The user explicitly cancelled the checkout. Navigate back or dismiss the checkout UI.
     */
    data object Cancelled : PrimerCheckoutState

    /**
     * Token created - process on your server (MANUAL flow).
     *
     * A payment method token was created and must be processed on your server.
     * After server processing, call [PrimerCheckoutController.resume] to continue the flow.
     *
     * @property token Payment method token to send to your server
     * @property paymentMethodType Type of payment method (e.g., "PAYMENT_CARD", "PAYPAL")
     */
    @Immutable
    data class TokenCreated(
        val token: String,
        val paymentMethodType: String,
    ) : PrimerCheckoutState
}
