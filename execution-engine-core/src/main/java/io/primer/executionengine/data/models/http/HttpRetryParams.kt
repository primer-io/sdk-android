package io.primer.executionengine.data.models.http

import io.primer.android.core.data.network.retry.RetryBackoff
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.optNullableInt
import io.primer.android.core.data.serialization.json.extensions.optNullableLong
import io.primer.android.core.data.serialization.json.extensions.optNullableString

/**
 * Wire model for the `retry` object of an `http.request` step. An absent `backoff` is `null`;
 * an unrecognized value is [RetryBackoff.UNKNOWN]. Both resolve to the exponential default.
 */
internal data class HttpRetryParams(
    val maxAttempts: Int?,
    val backoff: RetryBackoff?,
    val baseDelayMs: Long?,
    val retryOn: List<Int>?,
) : JSONDeserializable {

    companion object {
        private const val MAX_ATTEMPTS_FIELD = "maxAttempts"
        private const val BACKOFF_FIELD = "backoff"
        private const val BASE_DELAY_MS_FIELD = "baseDelayMs"
        private const val RETRY_ON_FIELD = "retryOn"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            HttpRetryParams(
                maxAttempts = json.optNullableInt(MAX_ATTEMPTS_FIELD),
                backoff = json.optNullableString(BACKOFF_FIELD)?.let { RetryBackoff.safeValueOf(it) },
                baseDelayMs = json.optNullableLong(BASE_DELAY_MS_FIELD),
                retryOn = json.optJSONArray(RETRY_ON_FIELD)?.let { array ->
                    List(array.length()) { index -> array.getInt(index) }
                },
            )
        }
    }
}
