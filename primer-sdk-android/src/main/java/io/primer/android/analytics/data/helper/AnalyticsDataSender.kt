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
        val groupedChunks =
            events.chunked(CHUNK_SIZE).map { it.groupBy { it.analyticsUrl ?: ANALYTICS_URL } }
        return groupedChunks.map { chunk ->
            chunk.map { group ->
                remoteAnalyticsDataSource.execute(
                    BaseRemoteUrlRequest(
                        group.key,
                        AnalyticsDataRequest(group.value.map { it.copy(newAnalyticsUrl = null) })
                    )
                ).map { chunk[group.key] }.retry(NUM_OF_RETRIES).catch {
                    emit(emptyList())
                }
            }
        }.flatten().merge().onEach { sentEvents ->
            localAnalyticsDataSource.remove(sentEvents.orEmpty())
        }.onCompletion {
            fileAnalyticsDataSource.update(localAnalyticsDataSource.get())
        }.map { }
    }

    private companion object {
        const val ANALYTICS_URL = "https://analytics.production.data.primer.io/sdk-logs"
        const val NUM_OF_RETRIES = 3L
        const val CHUNK_SIZE = 100
    }
}
