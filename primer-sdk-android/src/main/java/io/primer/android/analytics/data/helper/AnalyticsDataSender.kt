package io.primer.android.analytics.data.helper

import io.primer.android.analytics.data.datasource.LocalAnalyticsDataSource
import io.primer.android.analytics.data.datasource.RemoteAnalyticsDataSource
import io.primer.android.analytics.data.models.AnalyticsDataRequest
import io.primer.android.analytics.data.models.BaseAnalyticsEventRequest
import io.primer.android.analytics.infrastructure.datasource.FileAnalyticsDataSource
import io.primer.android.data.base.models.BaseRemoteUrlRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry

internal class AnalyticsDataSender(
    private val remoteAnalyticsDataSource: RemoteAnalyticsDataSource,
    private val localAnalyticsDataSource: LocalAnalyticsDataSource,
    private val fileAnalyticsDataSource: FileAnalyticsDataSource
) {

    fun sendEvents(events: List<BaseAnalyticsEventRequest>): Flow<Unit> {
        val grouped = events.groupBy { it.analyticsUrl ?: ANALYTICS_URL }
        return grouped.map {
            remoteAnalyticsDataSource.execute(
                BaseRemoteUrlRequest(
                    it.key,
                    AnalyticsDataRequest(it.value.map { it.copy(newAnalyticsUrl = null) })
                )
            ).map { grouped[it] }
                .retry(NUM_OF_RETRIES)
                .catch {
                    emit(emptyList())
                }
        }.merge()
            .onEach {
                localAnalyticsDataSource.remove(it.orEmpty())
            }.onCompletion {
                fileAnalyticsDataSource.update(localAnalyticsDataSource.get())
            }.map { }
    }

    private companion object {
        const val ANALYTICS_URL = "https://analytics.production.data.primer.io/sdk-logs"
        const val NUM_OF_RETRIES = 3L
    }
}
