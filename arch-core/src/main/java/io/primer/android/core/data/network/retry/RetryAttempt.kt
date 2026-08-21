package io.primer.android.core.data.network.retry

/**
 * A single scheduled retry, recorded in the retry history of a request.
 *
 * @param attempt the 1-based number of the failed attempt this retry was scheduled after.
 * @param delay the actual (clamped) delay in milliseconds before the next attempt.
 * @param error what caused the failed attempt.
 * @param timestamp epoch millis when the retry was scheduled.
 */
data class RetryAttempt(
    val attempt: Int,
    val delay: Long,
    val error: RetryAttemptError,
    val timestamp: Long,
)

sealed interface RetryAttemptError {
    data class Status(val statusCode: Int) : RetryAttemptError

    data class Thrown(val cause: Throwable) : RetryAttemptError
}
