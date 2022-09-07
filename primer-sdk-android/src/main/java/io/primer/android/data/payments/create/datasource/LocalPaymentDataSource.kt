package io.primer.android.data.payments.create.datasource

import io.primer.android.data.base.datasource.BaseCacheDataSource
import io.primer.android.data.payments.create.models.PaymentDataResponse

internal class LocalPaymentDataSource :
    BaseCacheDataSource<PaymentDataResponse, PaymentDataResponse> {
    private lateinit var paymentResponse: PaymentDataResponse

    override fun get(): PaymentDataResponse {
        return paymentResponse
    }

    override fun update(input: PaymentDataResponse) {
        paymentResponse = input
    }
}
