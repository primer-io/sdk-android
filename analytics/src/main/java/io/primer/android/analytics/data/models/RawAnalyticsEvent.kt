package io.primer.android.analytics.data.models

import io.primer.android.core.data.serialization.json.extensions.optNullableString
import org.json.JSONObject

/**
 * Lean analytics event that wraps a pre-formed JSON payload.
 * Used for events produced by external sources (e.g. BDC state processor)
 * that arrive already serialized and just need to flow through the pipeline.
 */
internal data class RawAnalyticsEvent(
    private val json: JSONObject,
) : AnalyticsEvent {

    override val analyticsUrl: String? = json.optNullableString(ANALYTICS_URL_FIELD)

    override fun withAnalyticsUrl(newAnalyticsUrl: String?): AnalyticsEvent {
        val copy = JSONObject(json.toString())
        if (newAnalyticsUrl != null) {
            copy.put(ANALYTICS_URL_FIELD, newAnalyticsUrl)
        } else {
            copy.remove(ANALYTICS_URL_FIELD)
        }
        return RawAnalyticsEvent(copy)
    }

    override fun toJson(): JSONObject = json

    companion object {
        private const val ANALYTICS_URL_FIELD = "analyticsUrl"

        fun fromJson(json: JSONObject): RawAnalyticsEvent = RawAnalyticsEvent(json)
    }
}
