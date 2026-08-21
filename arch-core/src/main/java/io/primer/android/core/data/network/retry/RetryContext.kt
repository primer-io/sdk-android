package io.primer.android.core.data.network.retry

import io.primer.android.core.data.network.transport.RawHttpResponse

/**
 * Snapshot handed to a [RetryStrategy] after a failed attempt.
 *
 * @param attempt 1-based count of failed attempts; the attempt that just failed is number [attempt].
 * @param response the failure of the attempt that just finished.
 * @param elapsedMs time elapsed since the first attempt started.
 * @param threw whether the attempt threw (instead of returning a transport-level failure).
 */
data class RetryContext(
    val attempt: Int,
    val response: HttpAttemptFailure,
    val elapsedMs: Long,
    val threw: Boolean,
)

sealed interface HttpAttemptFailure {
    /** The request settled with a non-2xx HTTP response. */
    data class SettledFailure(val response: RawHttpResponse) : HttpAttemptFailure

    /** The request never settled: the transport failed (or the attempt threw). */
    data class TransportFailure(val error: Throwable) : HttpAttemptFailure
}
