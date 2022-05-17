package io.primer.android.data.action.datasource

import io.primer.android.data.action.models.ClientSessionActionsDataRequest
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.models.Configuration
import io.primer.android.http.PrimerHttpClient

internal class RemoteActionDataSource(private val httpClient: PrimerHttpClient) :
    BaseFlowDataSource<Configuration, BaseRemoteRequest<ClientSessionActionsDataRequest>> {

    override fun execute(input: BaseRemoteRequest<ClientSessionActionsDataRequest>) =
        httpClient.post<ClientSessionActionsDataRequest, Configuration>(
            "${input.configuration.pciUrl}/client-session/actions",
            input.data
        )
}
