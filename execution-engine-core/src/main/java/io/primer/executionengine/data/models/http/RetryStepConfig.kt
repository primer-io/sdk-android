package io.primer.executionengine.data.models.http

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import org.json.JSONArray

internal enum class BackoffStrategy {
    EXPONENTIAL,
    LINEAR,
    CONSTANT,
}

internal data class RetryStepConfig(
    val maxAttempts: Int,
    val backoff: BackoffStrategy = BackoffStrategy.CONSTANT,
    val retryOn: List<Int> = emptyList(),
    val delay: Long = 0,
) : JSONDeserializable {

    companion object {
        private const val MAX_ATTEMPTS = "maxAttempts"
        private const val BACKOFF = "backoff"
        private const val RETRY_ON = "retryOn"
        private const val DELAY = "delay"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            val retryOnArray = json.optJSONArray(RETRY_ON) ?: JSONArray()
            val retryOn = List(retryOnArray.length()) { i ->
                retryOnArray.getInt(i)
            }

            RetryStepConfig(
                maxAttempts = json.getInt(MAX_ATTEMPTS),
                backoff = json.optString(BACKOFF, "constant")
                    .uppercase()
                    .let { BackoffStrategy.valueOf(it) },
                retryOn = retryOn,
                delay = json.optLong(DELAY, 0),
            )
        }
    }
}
