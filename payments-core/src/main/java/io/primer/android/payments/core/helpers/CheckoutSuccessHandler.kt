package io.primer.android.payments.core.helpers

import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.domain.payments.create.model.Payment
import kotlinx.coroutines.flow.Flow

/**
 * Dispatches checkout completion via callbacks. Exposes a flow that emits whenever checkout is completed.
 */
interface CheckoutSuccessHandler {
    /**
     * Flow that emits whenever checkout is completed.
     */
    val checkoutCompleted: Flow<Payment>

    /**
     * Dispatches checkout completion including the given [additional info][additionalInfo] if non null.
     */
    suspend fun handle(payment: Payment, additionalInfo: PrimerCheckoutAdditionalInfo?)
}
