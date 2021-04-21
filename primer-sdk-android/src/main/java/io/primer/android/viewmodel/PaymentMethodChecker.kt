package io.primer.android.viewmodel

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.ClientSession

internal interface PaymentMethodChecker {

    suspend fun shouldPaymentMethodBeAvailable(
        paymentMethod: PaymentMethod,
        clientSession: ClientSession,
    ): Boolean
}
