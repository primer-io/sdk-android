package io.primer.android.data.payments.resume.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.payments.create.models.PaymentDataResponse
import io.primer.android.data.payments.resume.models.ResumePaymentDataRequest
import io.primer.android.di.ApiVersion
import io.primer.android.di.SDK_API_VERSION_HEADER
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.Flow

internal class ResumePaymentDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseFlowDataSource<PaymentDataResponse,
        BaseRemoteRequest<Pair<String, ResumePaymentDataRequest>>> {

    override fun execute(input: BaseRemoteRequest<Pair<String, ResumePaymentDataRequest>>):
        Flow<PaymentDataResponse> {
        return primerHttpClient.post(
            "${input.configuration.pciUrl}/payments/${input.data.first}/resume",
            input.data.second,
            mapOf(
                SDK_API_VERSION_HEADER to ApiVersion.PAYMENTS_VERSION.version,
            )
        )
    }
}
