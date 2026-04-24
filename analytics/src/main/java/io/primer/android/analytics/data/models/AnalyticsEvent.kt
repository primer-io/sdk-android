package io.primer.android.analytics.data.models

import org.json.JSONObject

/**
 * Common contract for all analytics events flowing through the pipeline.
 * Implemented by [BaseAnalyticsEventRequest] for typed SDK events,
 * and [RawAnalyticsEvent] for pre-formed events from external sources (e.g. BDC state processor).
 */
internal interface AnalyticsEvent {
    val analyticsUrl: String?

    fun withAnalyticsUrl(newAnalyticsUrl: String?): AnalyticsEvent

    fun toJson(): JSONObject

    companion object {
        fun fromJson(json: JSONObject): AnalyticsEvent {
            return try {
                BaseAnalyticsEventRequest.deserializer.deserialize(json)
            } catch (_: Exception) {
                RawAnalyticsEvent.fromJson(json)
            }
        }
    }
}
