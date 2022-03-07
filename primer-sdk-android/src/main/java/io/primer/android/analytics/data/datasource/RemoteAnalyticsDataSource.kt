package io.primer.android.analytics.data.datasource

import io.primer.android.analytics.data.models.AnalyticsDataRequest
import io.primer.android.analytics.data.models.AnalyticsDataResponse
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteUrlRequest
import io.primer.android.http.PrimerHttpClient
import kotlinx.coroutines.flow.map

internal class RemoteAnalyticsDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseFlowDataSource<String, BaseRemoteUrlRequest<AnalyticsDataRequest>> {

    override fun execute(input: BaseRemoteUrlRequest<AnalyticsDataRequest>) =
        primerHttpClient.post<AnalyticsDataRequest, AnalyticsDataResponse>(
            input.url,
            input.data
        ).map { input.url }
}
