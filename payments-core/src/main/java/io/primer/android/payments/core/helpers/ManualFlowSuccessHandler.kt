package io.primer.android.payments.core.helpers

import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo

/**
 * Dispatches manual flow completion via callbacks.
 */
interface ManualFlowSuccessHandler {
    /**
     * Dispatches manual flow completion including.
     */
    suspend fun handle(additionalInfo: PrimerCheckoutAdditionalInfo? = null)
}
