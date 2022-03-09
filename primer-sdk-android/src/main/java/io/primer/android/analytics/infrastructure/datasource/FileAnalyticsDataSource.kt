package io.primer.android.analytics.infrastructure.datasource

import io.primer.android.analytics.infrastructure.files.AnalyticsFileProvider
import io.primer.android.analytics.data.models.BaseAnalyticsEventRequest
import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileOutputStream

internal class FileAnalyticsDataSource(
    private val fileProvider: AnalyticsFileProvider,
    private val json: Json,
) : BaseFlowCacheDataSource<List<BaseAnalyticsEventRequest>, List<BaseAnalyticsEventRequest>> {

    override fun get(): Flow<List<BaseAnalyticsEventRequest>> = flow {
        val bufferedReader =
            fileProvider.getFile(AnalyticsFileProvider.ANALYTICS_EVENTS_PATH).inputStream()
                .bufferedReader().use {
                    it.lineSequence().joinToString()
                }
        if (bufferedReader.isNotBlank()) emit(json.decodeFromString(bufferedReader))
    }

    override fun update(input: List<BaseAnalyticsEventRequest>) = synchronized(this) {
        val fileOutputStream = FileOutputStream(
            fileProvider.getFile(AnalyticsFileProvider.ANALYTICS_EVENTS_PATH)
        )
        fileOutputStream.write(json.encodeToString(input).toByteArray())
        fileOutputStream.use {
            it.flush()
        }
    }
}
