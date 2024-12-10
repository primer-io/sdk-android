package io.primer.android.payments.core.create.data.datasource

import io.primer.android.payments.core.create.data.model.PaymentDataResponse
import io.primer.android.core.data.datasource.BaseCacheDataSource

class LocalPaymentDataSource :
    BaseCacheDataSource<PaymentDataResponse, PaymentDataResponse> {
    private lateinit var paymentResponse: PaymentDataResponse

    override fun get(): PaymentDataResponse {
        return paymentResponse
    }

    override fun update(input: PaymentDataResponse) {
        paymentResponse = input
    }
}
