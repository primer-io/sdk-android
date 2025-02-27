package io.primer.android.analytics.infrastructure.datasource

import io.primer.android.analytics.data.models.BaseAnalyticsEventRequest
import io.primer.android.analytics.infrastructure.files.AnalyticsFileProvider
import io.primer.android.core.data.datasource.BaseFlowCacheDataSource
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.sequence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream

internal class FileAnalyticsDataSource(
    private val fileProvider: AnalyticsFileProvider,
) : BaseFlowCacheDataSource<List<BaseAnalyticsEventRequest>, List<BaseAnalyticsEventRequest>> {
    override fun get(): Flow<List<BaseAnalyticsEventRequest>> =
        flow {
            val content =
                fileProvider.getFile(AnalyticsFileProvider.ANALYTICS_EVENTS_PATH).inputStream()
                    .bufferedReader().use {
                        it.lineSequence().joinToString()
                    }
            if (content.isNotBlank()) {
                try {
                    emit(
                        JSONArray(content).sequence<JSONObject>().map {
                            JSONSerializationUtils
                                .getJsonObjectDeserializer<BaseAnalyticsEventRequest>()
                                .deserialize(it)
                        }.toList(),
                    )
                } catch (ignored: Exception) {
                    // if there is a problem while decoding file, then we do a hard reset!
                    update(listOf())
                }
            }
        }

    override fun update(input: List<BaseAnalyticsEventRequest>) =
        synchronized(this) {
            val fileOutputStream =
                FileOutputStream(
                    fileProvider.getFile(AnalyticsFileProvider.ANALYTICS_EVENTS_PATH),
                )
            fileOutputStream.use {
                it.write(
                    JSONArray().apply {
                        input.map { analyticsEvent ->
                            put(
                                JSONSerializationUtils
                                    .getJsonObjectSerializer<BaseAnalyticsEventRequest>()
                                    .serialize(analyticsEvent),
                            )
                        }
                    }.toString().toByteArray(),
                )
                it.flush()
            }
        }
}
