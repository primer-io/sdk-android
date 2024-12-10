package io.primer.android.payments.core.helpers

import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import kotlinx.coroutines.flow.Flow

/**
 * Dispatches [checkout additional info] via callbacks.
 */
interface CheckoutAdditionalInfoHandler {
    val checkoutAdditionalInfo: Flow<PrimerCheckoutAdditionalInfo>

    /**
     * Dispatches the given [additional info][checkoutAdditionalInfo], taking into consideration the
     * [PrimerCheckoutAdditionalInfo.completesCheckout] property and the payment handling type (auto or manual).
     */
    suspend fun handle(checkoutAdditionalInfo: PrimerCheckoutAdditionalInfo)
}
