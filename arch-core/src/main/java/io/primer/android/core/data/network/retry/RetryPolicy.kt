package io.primer.android.core.data.network.retry

/**
 * Caller-facing retry configuration. Every field is optional; a `null` field falls back to the
 * transport default (see [TransportRetryDefaults]) when resolved via [resolve].
 */
data class RetryPolicy(
    val maxAttempts: Int? = null,
    val backoff: RetryBackoff? = null,
    val baseDelayMs: Long? = null,
    val retryOn: List<Int>? = null,
    val totalTimeoutMs: Long? = null,
)

enum class RetryBackoff {
    EXPONENTIAL,
    FIXED,

    /** A backoff value the SDK does not recognize; resolves to [TransportRetryDefaults.BACKOFF]. */
    UNKNOWN,
    ;

    companion object {
        fun safeValueOf(value: String?): RetryBackoff =
            entries.firstOrNull { it.name == value?.uppercase() } ?: UNKNOWN
    }
}

/**
 * A [RetryPolicy] with every field resolved against [TransportRetryDefaults].
 * [retryOn] stays nullable: `null` means "retry every 5xx".
 */
data class ResolvedRetryPolicy(
    val maxAttempts: Int,
    val backoff: RetryBackoff,
    val baseDelayMs: Long,
    val retryOn: List<Int>?,
    val totalTimeoutMs: Long,
)

object TransportRetryDefaults {
    const val MAX_ATTEMPTS = 9
    val BACKOFF = RetryBackoff.EXPONENTIAL
    const val BASE_DELAY_MS = 100L
    const val TOTAL_TIMEOUT_MS = 300_000L
}

private const val MIN_ATTEMPTS = 1

/**
 * Resolves a possibly absent policy field-by-field against [TransportRetryDefaults].
 * A `null` receiver behaves like a policy with every field absent.
 */
fun RetryPolicy?.resolve(): ResolvedRetryPolicy =
    ResolvedRetryPolicy(
        maxAttempts = (this?.maxAttempts ?: TransportRetryDefaults.MAX_ATTEMPTS).coerceAtLeast(MIN_ATTEMPTS),
        backoff = this?.backoff ?: TransportRetryDefaults.BACKOFF,
        baseDelayMs = this?.baseDelayMs ?: TransportRetryDefaults.BASE_DELAY_MS,
        retryOn = this?.retryOn,
        totalTimeoutMs = this?.totalTimeoutMs ?: TransportRetryDefaults.TOTAL_TIMEOUT_MS,
    )
