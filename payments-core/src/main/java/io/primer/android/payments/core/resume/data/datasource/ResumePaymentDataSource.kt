package io.primer.android.payments.core.resume.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.Constants.SDK_API_VERSION_HEADER
import io.primer.android.payments.core.create.data.model.PaymentDataResponse
import io.primer.android.payments.core.resume.data.model.ResumePaymentDataRequest

internal class ResumePaymentDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseSuspendDataSource<PaymentDataResponse,
        BaseRemoteHostRequest<Pair<String, ResumePaymentDataRequest>>> {

    override suspend fun execute(input: BaseRemoteHostRequest<Pair<String, ResumePaymentDataRequest>>):
        PaymentDataResponse {
        return primerHttpClient.suspendPost<ResumePaymentDataRequest, PaymentDataResponse>(
            url = "${input.host}/payments/${input.data.first}/resume",
            request = input.data.second,
            headers = mapOf(SDK_API_VERSION_HEADER to PAYMENTS_VERSION)
        ).body
    }

    private companion object {
        const val PAYMENTS_VERSION = "2.2"
    }
}
