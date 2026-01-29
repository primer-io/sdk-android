package io.primer.android.components.analytics.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

/**
 * Datadog log event payload sent to /v1/sdk-logs endpoint.
 *
 * The message field contains a JSON-stringified LogMessageObject.
 */
internal data class LogEvent(
    val message: String, // JSON stringified LogMessageObject
    val hostname: String, // "android-app" or app identifier
    val service: String, // "android-sdk"
    val ddsource: String, // "lambda" (ingestion source)
    val ddtags: String, // "env:SANDBOX,version:2.0.0"
) : JSONObjectSerializable {

    companion object {
        @JvmField
        val serializer = JSONObjectSerializer<LogEvent> { event ->
            JSONObject().apply {
                put("message", event.message)
                put("hostname", event.hostname)
                put("service", event.service)
                put("ddsource", event.ddsource)
                put("ddtags", event.ddtags)
            }
        }
    }
}
