package io.primer.android.configuration.mock.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.model.EmptyDataRequest
import io.primer.android.core.data.model.EmptyDataResponse
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT

internal class RemoteFinalizeMockedFlowDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<EmptyDataResponse, BaseRemoteHostRequest<EmptyDataRequest>> {
    override suspend fun execute(input: BaseRemoteHostRequest<EmptyDataRequest>): EmptyDataResponse {
        return httpClient.withTimeout(PRIMER_60S_TIMEOUT)
            .suspendPost<EmptyDataRequest, EmptyDataResponse>(
                url = "${input.host}/finalize-polling",
                request = input.data,
            ).body
    }
}
