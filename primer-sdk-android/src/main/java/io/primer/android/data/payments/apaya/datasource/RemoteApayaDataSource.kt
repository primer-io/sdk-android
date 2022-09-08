package io.primer.android.data.payments.apaya.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.payments.apaya.models.CreateSessionDataRequest
import io.primer.android.data.payments.apaya.models.CreateSessionDataResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteApayaDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseFlowDataSource<CreateSessionDataResponse, BaseRemoteRequest<CreateSessionDataRequest>> {

    override fun execute(input: BaseRemoteRequest<CreateSessionDataRequest>) =
        primerHttpClient.post<CreateSessionDataRequest, CreateSessionDataResponse>(
            "${input.configuration.coreUrl}/session-token",
            input.data
        )
}
