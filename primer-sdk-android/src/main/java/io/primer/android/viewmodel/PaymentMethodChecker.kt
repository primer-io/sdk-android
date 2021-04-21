package io.primer.android.viewmodel

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.ClientSession

/**
 * A PaymentMethodChecker is responsible for evaluating if a given [PaymentMethod] is available or
 * not, at run-time. See [PaymentMethodCheckerRegistry].
 */
internal interface PaymentMethodChecker {

    suspend fun shouldPaymentMethodBeAvailable(
        paymentMethod: PaymentMethod,
        clientSession: ClientSession,
    ): Boolean
}
