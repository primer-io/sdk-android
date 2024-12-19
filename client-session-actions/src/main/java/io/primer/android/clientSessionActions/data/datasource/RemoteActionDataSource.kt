package io.primer.android.clientSessionActions.data.datasource

import io.primer.android.clientSessionActions.data.models.ClientSessionActionsDataRequest
import io.primer.android.configuration.data.model.ConfigurationDataResponse
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.PrimerResponse
import io.primer.android.core.data.network.utils.Constants

internal class RemoteActionDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<PrimerResponse<ConfigurationDataResponse>,
        BaseRemoteHostRequest<ClientSessionActionsDataRequest>> {

    override suspend fun execute(input: BaseRemoteHostRequest<ClientSessionActionsDataRequest>) =
        httpClient.suspendPost<ClientSessionActionsDataRequest, ConfigurationDataResponse>(
            url = "${input.host}/client-session/actions",
            request = input.data,
            headers = mapOf(Constants.SDK_API_VERSION_HEADER to CLIENT_SESSION_ACTIONS_VERSION)
        )

    private companion object {

        const val CLIENT_SESSION_ACTIONS_VERSION = "2.3"
    }
}
