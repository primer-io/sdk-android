package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateSessionDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateSessionDataResponse
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.Flow

internal class RemoteKlarnaSessionDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseFlowDataSource<CreateSessionDataResponse, BaseRemoteRequest<CreateSessionDataRequest>> {
    override fun execute(input: BaseRemoteRequest<CreateSessionDataRequest>):
        Flow<CreateSessionDataResponse> {
        return primerHttpClient.post(
            "${input.configuration.coreUrl}/klarna/payment-sessions",
            input.data
        )
    }
}
