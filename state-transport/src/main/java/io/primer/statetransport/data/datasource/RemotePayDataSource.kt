package io.primer.statetransport.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.PrimerResponse
import io.primer.statetransport.data.model.ClientSessionInstructionResponse
import io.primer.statetransport.data.model.ClientSessionPayDataRequest

internal class RemotePayDataSource(
    private val httpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : BaseSuspendDataSource<
    PrimerResponse<ClientSessionInstructionResponse>,
    BaseRemoteHostRequest<ClientSessionPayDataRequest>,
    > {
    override suspend fun execute(input: BaseRemoteHostRequest<ClientSessionPayDataRequest>) =
        httpClient.suspendPost<ClientSessionPayDataRequest, ClientSessionInstructionResponse>(
            url = input.host,
            request = input.data,
            headers = apiVersion().toHeaderMap(),
        )
}
