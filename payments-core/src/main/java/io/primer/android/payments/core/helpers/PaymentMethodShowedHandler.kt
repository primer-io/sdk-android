package io.primer.android.payments.core.helpers

import kotlinx.coroutines.flow.Flow

/**
 * Dispatches payment method showed event via callbacks. Exposes a flow that emits whenever payment method is shown.
 */
interface PaymentMethodShowedHandler {
    /**
     * Flow that emits the payment method type whenever the payment method is shown.
     */
    val paymentMethodShowed: Flow<String>

    /**
     * Dispatches the event including the given [paymentMethodType].
     */
    suspend fun handle(paymentMethodType: String)
}
