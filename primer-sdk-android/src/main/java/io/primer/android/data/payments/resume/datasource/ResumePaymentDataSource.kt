package io.primer.android.data.payments.resume.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.payments.create.models.PaymentResponse
import io.primer.android.data.payments.resume.models.ResumePaymentRequest
import io.primer.android.di.ApiVersion
import io.primer.android.di.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.Flow

internal class ResumePaymentDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseFlowDataSource<PaymentResponse, BaseRemoteRequest<Pair<String, ResumePaymentRequest>>> {

    override fun execute(input: BaseRemoteRequest<Pair<String, ResumePaymentRequest>>):
        Flow<PaymentResponse> {
        return primerHttpClient.post(
            "${input.configuration.pciUrl}/payments/${input.data.first}/resume",
            input.data.second,
            mapOf(
                SDK_API_VERSION_HEADER to ApiVersion.PAYMENTS_VERSION.version,
            )
        )
    }
}
