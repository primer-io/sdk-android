package io.primer.android.payments.core.create.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.Constants.SDK_API_VERSION_HEADER
import io.primer.android.payments.core.create.data.model.CreatePaymentDataRequest
import io.primer.android.payments.core.create.data.model.PaymentDataResponse

internal class CreatePaymentDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseSuspendDataSource<PaymentDataResponse, BaseRemoteHostRequest<CreatePaymentDataRequest>> {

    override suspend fun execute(input: BaseRemoteHostRequest<CreatePaymentDataRequest>):
        PaymentDataResponse {
        return primerHttpClient.suspendPost<CreatePaymentDataRequest, PaymentDataResponse>(
            url = "${input.host}/payments",
            request = input.data,
            headers = mapOf(
                SDK_API_VERSION_HEADER to PAYMENTS_VERSION,
                HEADER_ACCEPT to "*/*"
            )
        ).body
    }

    private companion object {
        const val PAYMENTS_VERSION = "2.2"
        const val HEADER_ACCEPT = "accept"
    }
}
