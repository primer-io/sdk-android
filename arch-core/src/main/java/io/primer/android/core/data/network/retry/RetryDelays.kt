package io.primer.android.core.data.network.retry

/** Hard cap for a single backoff window. */
internal const val MAX_DELAY_MS = 30_000L

private const val JITTER_WINDOW_DIVISOR = 2.0
private const val BACKOFF_MULTIPLIER = 2

/**
 * Applies half-fixed/half-random jitter to a delay window: the result is uniformly distributed in
 * `[windowMs / 2, windowMs]`. Mandatory for built-in delays; exported so custom strategies can compose it.
 */
fun jitteredDelayMs(
    windowMs: Long,
    random: () -> Double = Math::random,
): Long = Math.round(windowMs / JITTER_WINDOW_DIVISOR + random() * (windowMs / JITTER_WINDOW_DIVISOR))

/**
 * Computes the jittered backoff delay for the given number of [failedAttempts] (1-based).
 * The window grows exponentially from [ResolvedRetryPolicy.baseDelayMs] (or stays flat for
 * [RetryBackoff.FIXED]) and is capped at [MAX_DELAY_MS] before jitter is applied.
 */
fun backoffDelayMs(
    policy: ResolvedRetryPolicy,
    failedAttempts: Int,
    random: () -> Double = Math::random,
): Long {
    val windowMs =
        when (policy.backoff) {
            RetryBackoff.FIXED -> policy.baseDelayMs
            // UNKNOWN is resolved to exponential upstream; kept here for a total, defensive when.
            RetryBackoff.EXPONENTIAL, RetryBackoff.UNKNOWN ->
                exponentialWindowMs(policy.baseDelayMs, failedAttempts)
        }.coerceAtMost(MAX_DELAY_MS)
    return jitteredDelayMs(windowMs, random)
}

private fun exponentialWindowMs(
    baseDelayMs: Long,
    failedAttempts: Int,
): Long {
    var windowMs = baseDelayMs
    repeat(failedAttempts - 1) {
        if (windowMs >= MAX_DELAY_MS) return MAX_DELAY_MS
        windowMs *= BACKOFF_MULTIPLIER
    }
    return windowMs
}
