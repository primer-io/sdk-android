package io.primer.android.configuration.mock.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.model.EmptyDataRequest
import io.primer.android.core.data.model.EmptyDataResponse
import io.primer.android.core.data.network.PrimerHttpClient

internal class RemoteFinalizeMockedFlowDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<EmptyDataResponse, BaseRemoteHostRequest<EmptyDataRequest>> {
    override suspend fun execute(input: BaseRemoteHostRequest<EmptyDataRequest>): EmptyDataResponse {
        return httpClient.suspendPost<EmptyDataRequest, EmptyDataResponse>(
            "${input.host}/finalize-polling",
            input.data,
        ).body
    }
}
