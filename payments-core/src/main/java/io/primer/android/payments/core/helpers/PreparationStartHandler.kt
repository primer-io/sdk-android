package io.primer.android.payments.core.helpers

import kotlinx.coroutines.flow.Flow

/**
 * Dispatches preparation start event via callbacks. Exposes a flow that emits whenever preparation is started.
 */
interface PreparationStartHandler {
    /**
     * Flow that emits the payment method type whenever preparation is started.
     */
    val preparationStarted: Flow<String>

    /**
     * Dispatches the event including the given [paymentMethodType].
     */
    suspend fun handle(paymentMethodType: String)
}
