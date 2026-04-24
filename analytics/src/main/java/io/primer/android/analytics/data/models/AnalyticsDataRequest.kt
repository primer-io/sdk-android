package io.primer.android.analytics.data.models

import io.primer.android.core.data.serialization.json.JSONArraySerializable
import io.primer.android.core.data.serialization.json.JSONArraySerializer
import org.json.JSONArray

internal data class AnalyticsDataRequest(val data: List<AnalyticsEvent>) :
    JSONArraySerializable {
    companion object {
        @JvmField
        val serializer =
            object : JSONArraySerializer<AnalyticsDataRequest> {
                override fun serialize(t: AnalyticsDataRequest): JSONArray {
                    return JSONArray().apply {
                        t.data.map { put(it.toJson()) }
                    }
                }
            }
    }
}
