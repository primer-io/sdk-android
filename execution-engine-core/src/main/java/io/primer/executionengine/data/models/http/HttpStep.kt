package io.primer.executionengine.data.models.http

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.toMap
import io.primer.android.core.data.serialization.json.extensions.toStringMap

internal data class HttpStep(
    val type: String,
    val url: String,
    val method: HttpMethod = HttpMethod.GET,
    val body: Map<String, Any?>? = null,
    val headers: Map<String, String>? = null,
    val query: Map<String, String>? = null,
    val retry: RetryStepConfig? = null,
    val timeout: Int? = null,
    val state: Map<String, Any?>? = null,
) : JSONDeserializable {

    companion object {
        private const val TYPE = "type"
        private const val URL = "url"
        private const val METHOD = "method"
        private const val BODY = "body"
        private const val HEADERS = "headers"
        private const val QUERY = "query"
        private const val RETRY = "retry"
        private const val TIMEOUT = "timeout"
        private const val STATE = "state"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            HttpStep(
                type = json.getString(TYPE),
                url = json.getString(URL),
                method = json.optString(METHOD, "GET")
                    .uppercase()
                    .let { HttpMethod.valueOf(it) },
                body = json.optJSONObject(BODY)?.toMap(),
                headers = json.optJSONObject(HEADERS)?.toStringMap(),
                query = json.optJSONObject(QUERY)?.toStringMap(),
                retry = json.optJSONObject(RETRY)?.let {
                    RetryStepConfig.deserializer.deserialize(it)
                },
                timeout = if (json.has(TIMEOUT) && !json.isNull(TIMEOUT)) json.getInt(TIMEOUT) else null,
                state = json.optJSONObject(STATE)?.toMap(),
            )
        }
    }
}
