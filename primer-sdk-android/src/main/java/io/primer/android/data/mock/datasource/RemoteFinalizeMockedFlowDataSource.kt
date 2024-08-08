package io.primer.android.data.mock.datasource

import io.primer.android.core.data.models.EmptyDataRequest
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RemoteFinalizeMockedFlowDataSource(private val httpClient: PrimerHttpClient) :
    BaseFlowDataSource<EmptyDataRequest, BaseRemoteRequest<EmptyDataRequest>> {

    override fun execute(input: BaseRemoteRequest<EmptyDataRequest>): Flow<EmptyDataRequest> {
        return httpClient.post<EmptyDataRequest, EmptyDataRequest>(
            "${input.configuration.coreUrl}/finalize-polling",
            input.data
        ).map { responseData -> responseData.body }
    }
}
