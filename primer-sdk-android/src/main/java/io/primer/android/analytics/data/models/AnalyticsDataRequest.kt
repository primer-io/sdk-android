package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONArray
import org.json.JSONObject

internal data class AnalyticsDataRequest(val data: List<BaseAnalyticsEventRequest>) :
    JSONSerializable {

    companion object {
        private const val DATA_FIELD = "data"

        @JvmField
        val serializer = object : JSONSerializer<AnalyticsDataRequest> {
            override fun serialize(t: AnalyticsDataRequest): JSONObject {
                return JSONObject().apply {
                    put(
                        DATA_FIELD,
                        JSONArray().apply {
                            put(
                                t.data.map {
                                    JSONSerializationUtils
                                        .getSerializer<BaseAnalyticsEventRequest>()
                                        .serialize(it)
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}
