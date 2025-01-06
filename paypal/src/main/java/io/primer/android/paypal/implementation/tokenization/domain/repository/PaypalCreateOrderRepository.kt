package io.primer.android.paypal.implementation.tokenization.domain.repository

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateOrderParams
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrder

internal fun interface PaypalCreateOrderRepository {
    suspend fun createOrder(params: PaypalCreateOrderParams): Result<PaypalOrder>
}
