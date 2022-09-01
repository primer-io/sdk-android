package io.primer.android.data.payments.create.datasource

import io.primer.android.data.base.datasource.BaseCacheDataSource
import io.primer.android.data.payments.create.models.PaymentResponse

internal class LocalPaymentDataSource : BaseCacheDataSource<PaymentResponse, PaymentResponse> {
    private lateinit var paymentResponse: PaymentResponse

    override fun get(): PaymentResponse {
        return paymentResponse
    }

    override fun update(input: PaymentResponse) {
        paymentResponse = input
    }
}
