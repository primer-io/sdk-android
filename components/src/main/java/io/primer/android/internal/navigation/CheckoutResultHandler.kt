package io.primer.android.internal.navigation

import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError

/**
 * Handles checkout result delivery to the merchant.
 *
 * Implementations are responsible for:
 * - Showing success/error UI if needed
 * - Delivering results to merchant callbacks
 * - Managing any delay before result delivery (e.g., success screen display time)
 */
internal interface CheckoutResultHandler {
    /**
     * Called when checkout completes successfully.
     * Shows success UI and schedules result delivery.
     */
    fun onSuccess(checkoutData: PrimerCheckoutData)

    /**
     * Called when checkout fails.
     * Delivers error to merchant callback.
     *
     * @param error The Primer error with full details (errorId, diagnosticsId, etc.)
     */
    fun onError(error: PrimerError)
}
