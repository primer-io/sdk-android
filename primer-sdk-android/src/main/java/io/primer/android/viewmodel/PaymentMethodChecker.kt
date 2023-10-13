package io.primer.android.viewmodel

import io.primer.android.PaymentMethod

/**
 * A PaymentMethodChecker is responsible for evaluating if a given [PaymentMethod] is available or
 * not, at run-time. See [PaymentMethodCheckerRegistry].
 */
internal interface PaymentMethodChecker {

    suspend fun shouldPaymentMethodBeAvailable(
        paymentMethod: PaymentMethod
    ): Boolean
}
