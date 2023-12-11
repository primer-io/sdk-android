package io.primer.android.analytics.data.helper

import androidx.annotation.WorkerThread
import io.primer.android.analytics.data.datasource.RemoteAnalyticsDataSource
import io.primer.android.analytics.data.models.AnalyticsDataRequest
import io.primer.android.analytics.data.models.BaseAnalyticsEventRequest
import io.primer.android.data.base.models.BaseRemoteUrlRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.retry

internal class AnalyticsDataSender(
    private val remoteAnalyticsDataSource: RemoteAnalyticsDataSource
) {
    @WorkerThread
    fun sendEvents(events: List<BaseAnalyticsEventRequest>): Flow<List<BaseAnalyticsEventRequest>> {
        val groupedChunks =
            events.chunked(CHUNK_SIZE)
                .map { chunked -> chunked.groupBy { it.analyticsUrl ?: ANALYTICS_URL } }
        return groupedChunks.map { chunk ->
            chunk.map { group ->
                remoteAnalyticsDataSource.execute(
                    BaseRemoteUrlRequest(
                        group.key,
                        AnalyticsDataRequest(group.value.map { it.copy(newAnalyticsUrl = null) })
                    )
                ).map { chunk[group.key].orEmpty() }.retry(NUM_OF_RETRIES).catch {
                    emit(emptyList())
                }
            }
        }.flatten().merge()
    }

    private companion object {
        const val ANALYTICS_URL = "https://analytics.production.data.primer.io/sdk-logs"
        const val NUM_OF_RETRIES = 3L
        const val CHUNK_SIZE = 100
    }
}
