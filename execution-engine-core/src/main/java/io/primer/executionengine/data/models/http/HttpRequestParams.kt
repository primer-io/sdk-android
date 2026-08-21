package io.primer.executionengine.data.models.http

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.optNullableLong
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import org.json.JSONObject

/**
 * Model for an `http.request` step. Unknown extra fields in incoming JSON are ignored.
 *
 * @property body the raw `org.json` value ([JSONObject], [org.json.JSONArray], [String], [Number],
 * [Boolean], or `null`); an explicit JSON `null` is stored as `null`.
 */
internal data class HttpRequestParams(
    val method: String,
    val url: String,
    val body: Any?,
    val timeoutMs: Long?,
    val retry: HttpRetryParams?,
    val idempotencyKey: String?,
) : JSONDeserializable {

    companion object {
        private const val METHOD_FIELD = "method"
        private const val URL_FIELD = "url"
        private const val BODY_FIELD = "body"
        private const val TIMEOUT_MS_FIELD = "timeoutMs"
        private const val RETRY_FIELD = "retry"
        private const val IDEMPOTENCY_KEY_FIELD = "idempotencyKey"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            HttpRequestParams(
                method = json.getString(METHOD_FIELD),
                url = json.getString(URL_FIELD),
                body = json.opt(BODY_FIELD).takeUnless { it == JSONObject.NULL },
                timeoutMs = json.optNullableLong(TIMEOUT_MS_FIELD),
                retry = json.optJSONObject(RETRY_FIELD)?.let { HttpRetryParams.deserializer.deserialize(it) },
                idempotencyKey = json.optNullableString(IDEMPOTENCY_KEY_FIELD),
            )
        }
    }
}
