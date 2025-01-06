package io.primer.android.analytics.data.models

import io.primer.android.core.data.serialization.json.JSONArraySerializable
import io.primer.android.core.data.serialization.json.JSONArraySerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONArray

internal data class AnalyticsDataRequest(val data: List<BaseAnalyticsEventRequest>) :
    JSONArraySerializable {
    companion object {
        @JvmField
        val serializer =
            object : JSONArraySerializer<AnalyticsDataRequest> {
                override fun serialize(t: AnalyticsDataRequest): JSONArray {
                    return JSONArray().apply {
                        t.data.map {
                            put(
                                JSONSerializationUtils
                                    .getJsonObjectSerializer<BaseAnalyticsEventRequest>()
                                    .serialize(it),
                            )
                        }
                    }
                }
            }
    }
}
