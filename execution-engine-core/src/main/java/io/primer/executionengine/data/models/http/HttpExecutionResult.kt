package io.primer.executionengine.data.models.http

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class HttpExecutionResult(
    val statusCode: Int? = null,
    val response: String? = null,
    val headers: Map<String, List<String>> = emptyMap(),
) : JSONObjectSerializable {

    companion object {
        private const val STATUS_CODE_FIELD = "statusCode"
        private const val RESPONSE_FIELD = "response"
        private const val HEADERS_FIELD = "headers"

        @JvmField
        val serializer = JSONObjectSerializer<HttpExecutionResult> { t ->
            JSONObject().apply {
                put(STATUS_CODE_FIELD, t.statusCode)
                put(RESPONSE_FIELD, t.response)
                put(HEADERS_FIELD, t.headers)
            }
        }
    }
}
