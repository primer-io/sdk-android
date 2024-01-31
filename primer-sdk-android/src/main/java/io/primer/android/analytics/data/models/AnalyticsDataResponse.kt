package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import org.json.JSONObject

internal data class AnalyticsDataResponse(val result: String) : JSONDeserializable {

    companion object {
        private const val RESULT_FIELD = "result"

        @JvmField
        val deserializer = object : JSONObjectDeserializer<AnalyticsDataResponse> {

            override fun deserialize(t: JSONObject): AnalyticsDataResponse {
                return AnalyticsDataResponse(t.getString(RESULT_FIELD))
            }
        }
    }
}
