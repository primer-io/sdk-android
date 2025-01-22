package io.primer.android.clientSessionActions.data.datasource

import io.primer.android.clientSessionActions.data.models.ClientSessionActionsDataRequest
import io.primer.android.configuration.data.model.ConfigurationDataResponse
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.PrimerResponse
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT

internal class RemoteActionDataSource(
    private val httpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : BaseSuspendDataSource<
        PrimerResponse<ConfigurationDataResponse>,
        BaseRemoteHostRequest<ClientSessionActionsDataRequest>,
        > {
    override suspend fun execute(input: BaseRemoteHostRequest<ClientSessionActionsDataRequest>) =
        httpClient.withTimeout(PRIMER_15S_TIMEOUT)
            .suspendPost<ClientSessionActionsDataRequest, ConfigurationDataResponse>(
                url = "${input.host}/client-session/actions",
                request = input.data,
                headers = apiVersion().toHeaderMap(),
            )
}
