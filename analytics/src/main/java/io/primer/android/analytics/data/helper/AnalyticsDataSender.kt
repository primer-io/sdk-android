package io.primer.android.analytics.data.helper

import androidx.annotation.WorkerThread
import io.primer.android.analytics.data.datasource.RemoteAnalyticsDataSource
import io.primer.android.analytics.data.models.AnalyticsDataRequest
import io.primer.android.analytics.data.models.AnalyticsEvent
import io.primer.android.core.data.model.BaseRemoteUrlRequest
import io.primer.android.core.data.network.utils.isPrimerUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.retry

internal class AnalyticsDataSender(
    private val remoteAnalyticsDataSource: RemoteAnalyticsDataSource,
) {
    /**
     * Events whose URL is not a Primer host are reported as sent without a request, so callers drop
     * them from memory and disk. They can only come from a forged client token persisted by an older
     * SDK (ORC-9418).
     */
    @WorkerThread
    fun sendEvents(events: List<AnalyticsEvent>): Flow<List<AnalyticsEvent>> {
        val flows = mutableListOf<Flow<List<AnalyticsEvent>>>()

        val trusted = events.filter { it.targetUrl.isPrimerUrl() }
        for (chunk in trusted.chunked(CHUNK_SIZE)) {
            for ((url, sameUrlEvents) in chunk.groupBy { it.targetUrl }) {
                flows += send(url, sameUrlEvents)
            }
        }

        val untrusted = events.filterNot { it.targetUrl.isPrimerUrl() }
        if (untrusted.isNotEmpty()) flows += flowOf(untrusted)

        return flows.merge()
    }

    private val AnalyticsEvent.targetUrl: String
        get() = analyticsUrl ?: ANALYTICS_URL

    /** Emits [events] once the request succeeds, or an empty list when it still fails after the retries. */
    private fun send(
        url: String,
        events: List<AnalyticsEvent>,
    ): Flow<List<AnalyticsEvent>> =
        remoteAnalyticsDataSource.execute(
            BaseRemoteUrlRequest(
                url = url,
                data = AnalyticsDataRequest(events.map { it.withAnalyticsUrl(null) }),
            ),
        ).map { events }.retry(NUM_OF_RETRIES).catch { emit(emptyList()) }

    private companion object {
        const val ANALYTICS_URL = "https://analytics.production.data.primer.io/sdk-logs"
        const val NUM_OF_RETRIES = 3L
        const val CHUNK_SIZE = 100
    }
}
