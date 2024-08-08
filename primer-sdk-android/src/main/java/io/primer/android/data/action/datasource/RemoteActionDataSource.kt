package io.primer.android.data.action.datasource

import io.primer.android.data.action.models.ClientSessionActionsDataRequest
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.models.ConfigurationDataResponse
import io.primer.android.di.ApiVersion
import io.primer.android.di.NetworkContainer
import io.primer.android.http.PrimerHttpClient
import io.primer.android.http.PrimerResponse

internal class RemoteActionDataSource(private val httpClient: PrimerHttpClient) :
    BaseFlowDataSource<PrimerResponse<ConfigurationDataResponse>,
        BaseRemoteRequest<ClientSessionActionsDataRequest>> {

    override fun execute(input: BaseRemoteRequest<ClientSessionActionsDataRequest>) =
        httpClient.post<ClientSessionActionsDataRequest, ConfigurationDataResponse>(
            "${input.configuration.pciUrl}/client-session/actions",
            input.data,
            mapOf(
                NetworkContainer.SDK_API_VERSION_HEADER to
                    ApiVersion.CLIENT_SESSION_ACTIONS_VERSION.version
            )
        )
}
