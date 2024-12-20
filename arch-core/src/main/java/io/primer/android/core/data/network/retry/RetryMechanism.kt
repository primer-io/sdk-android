package io.primer.android.core.data.network.retry

import io.primer.android.core.data.network.helpers.MessageLog
import io.primer.android.core.data.network.helpers.MessagePropertiesHelper
import io.primer.android.core.data.network.helpers.MessageTypeHelper
import io.primer.android.core.data.network.helpers.SeverityHelper
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlin.math.min
import kotlin.math.pow

/** Maximum number of retry attempts */
internal const val MAX_RETRIES = 8

/** Initial backoff time in milliseconds */
internal const val INITIAL_BACKOFF = 100

/** Maximum jitter in milliseconds */
internal const val MAX_JITTER = 100

const val NETWORK_EXCEPTION_ERROR_CODE = 1001

internal const val SERVER_ERROR_FIRST = 500

internal const val SERVER_ERROR_LAST = 599

internal const val BAD_REQUEST_START = 400

internal const val BAD_REQUEST_END = 499

val SERVER_ERRORS = SERVER_ERROR_FIRST..SERVER_ERROR_LAST

internal val BAD_REQUEST_ERRORS = BAD_REQUEST_START..BAD_REQUEST_END

suspend fun retry(
    response: Response,
    config: RetryConfig,
    logProvider: EventFlowProvider<MessageLog>,
    messagePropertiesEventProvider: EventFlowProvider<MessagePropertiesHelper>
): Boolean {
    val canRetry = config.canRetry()
    val delayTime = config.calculateBackoffWithJitter()
    val isNetworkError = response.code == NETWORK_EXCEPTION_ERROR_CODE
    val isServerError = response.code in SERVER_ERRORS
    val isBadRequest = response.code in BAD_REQUEST_ERRORS

    if (
        response.isSuccessful ||
        !config.enabled ||
        (!config.retryNetworkErrors && isNetworkError) ||
        (!config.retry500Errors && isServerError) ||
        isBadRequest
    ) {
        return false
    }

    if (canRetry) {
        config.retries++
        val reason = when {
            isNetworkError ->
                "Network error encountered and " +
                    "RetryConfig.retryNetworkErrors is ${if (config.retryNetworkErrors) "enabled" else "disabled"}"

            isServerError ->
                "HTTP ${response.code} error encountered and " +
                    "RetryConfig.retry500Errors is ${if (config.retry500Errors) "enabled" else "disabled"}"

            else ->
                "HTTP ${response.code} error encountered"
        }
        val message =
            "Retry attempt ${config.retries}/${config.maxRetries} due to $reason. " +
                "Waiting for ${delayTime}ms before next attempt."
        logProvider.getEventProvider().emit(MessageLog(message = message, severity = SeverityHelper.WARN))
        messagePropertiesEventProvider.getEventProvider().tryEmit(
            MessagePropertiesHelper(
                MessageTypeHelper.RETRY,
                message,
                SeverityHelper.WARN
            )
        )
        delay(delayTime)
    }

    return canRetry
}

internal fun RetryConfig.calculateBackoffWithJitter(): Long {
    val exponentialPart = INITIAL_BACKOFF * 2.0.pow(retries - 1)
    val jitterPart = Math.random() * MAX_JITTER
    val backoff = (exponentialPart + jitterPart).toLong()
    return min(backoff, Long.MAX_VALUE)
}

fun networkError(url: String) = Response.Builder()
    .request(
        Request.Builder()
            .url(url)
            .build()
    )
    .protocol(Protocol.HTTP_2)
    .code(NETWORK_EXCEPTION_ERROR_CODE)
    .body(
        """
            {
                "description":"Network error encountered when retrying"
            }
        """.trimIndent().toResponseBody("application/json; charset=utf-8".toMediaType())
    )
    .message("")
    .build()

internal fun RetryConfig.canRetry() = retries < maxRetries

fun RetryConfig.isLastAttempt() = retries == maxRetries
