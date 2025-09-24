package io.primer.android.components.analytics.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import org.json.JSONObject

/**
 * Represents the response from the Primer Analytics API after submitting an analytics event.
 *
 * This data class encapsulates the server's acknowledgment of a received analytics event,
 * providing confirmation that the event has been successfully received and processed.
 *
 * @property id Optional identifier returned by the server for tracking purposes
 * @property result Processing status of the analytics event (e.g., "processed")
 *
 * Example JSON response:
 * ```
 * {
 *   "id": null,
 *   "result": "processed"
 * }
 * ```
 */
data class AnalyticsResponse(
    val id: String?,
    val result: String?,
) : JSONDeserializable {
    companion object {
        @JvmField
        val deserializer = JSONObjectDeserializer<AnalyticsResponse> { json: JSONObject ->
            AnalyticsResponse(
                id = json.optString("id").takeIf { it.isNotEmpty() },
                result = json.optString("result").takeIf { it.isNotEmpty() },
            )
        }
    }
}
