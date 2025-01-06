package io.primer.android.analytics.data.datasource

import io.primer.android.analytics.data.models.AnalyticsDataRequest
import io.primer.android.analytics.data.models.AnalyticsDataResponse
import io.primer.android.core.data.datasource.BaseFlowDataSource
import io.primer.android.core.data.model.BaseRemoteUrlRequest
import io.primer.android.core.data.network.PrimerHttpClient
import kotlinx.coroutines.flow.map

internal class RemoteAnalyticsDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseFlowDataSource<String, BaseRemoteUrlRequest<AnalyticsDataRequest>> {
    override fun execute(input: BaseRemoteUrlRequest<AnalyticsDataRequest>) =
        primerHttpClient.post<AnalyticsDataRequest, AnalyticsDataResponse>(
            input.url,
            input.data,
        ).map { input.url }
}
