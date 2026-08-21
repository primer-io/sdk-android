package io.primer.executionengine.data.models.http

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

/**
 * The `http.request` step output exposed to the engine: available as step data after both
 * success and error outcomes.
 */
internal data class HttpResponseOutput(
    val status: Int,
    val success: Boolean,
    val headers: Map<String, String>,
    val body: Any?,
) : JSONObjectSerializable {

    fun toDataMap(): Map<String, Any?> =
        mapOf(
            STATUS_FIELD to status,
            SUCCESS_FIELD to success,
            HEADERS_FIELD to headers,
            BODY_FIELD to body,
        )

    companion object {
        private const val STATUS_FIELD = "status"
        private const val SUCCESS_FIELD = "success"
        private const val HEADERS_FIELD = "headers"
        private const val BODY_FIELD = "body"

        @JvmField
        val serializer = JSONObjectSerializer<HttpResponseOutput> { t ->
            JSONObject().apply {
                put(STATUS_FIELD, t.status)
                put(SUCCESS_FIELD, t.success)
                put(HEADERS_FIELD, JSONObject(t.headers))
                put(BODY_FIELD, JSONObject.wrap(t.body) ?: JSONObject.NULL)
            }
        }
    }
}
