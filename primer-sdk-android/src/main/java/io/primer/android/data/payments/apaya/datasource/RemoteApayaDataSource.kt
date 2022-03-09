package io.primer.android.data.payments.apaya.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.payments.apaya.models.CreateSessionRequest
import io.primer.android.data.payments.apaya.models.CreateSessionResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteApayaDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseFlowDataSource<CreateSessionResponse, BaseRemoteRequest<CreateSessionRequest>> {

    override fun execute(input: BaseRemoteRequest<CreateSessionRequest>) =
        primerHttpClient.post<CreateSessionRequest, CreateSessionResponse>(
            "${input.configuration.coreUrl}/session-token",
            input.data
        )
}
