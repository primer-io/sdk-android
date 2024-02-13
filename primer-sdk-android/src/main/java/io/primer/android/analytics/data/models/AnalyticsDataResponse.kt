package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer

internal data class AnalyticsDataResponse(val result: String) : JSONDeserializable {

    companion object {
        private const val RESULT_FIELD = "result"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t -> AnalyticsDataResponse(t.getString(RESULT_FIELD)) }
    }
}
