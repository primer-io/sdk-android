package io.primer.android.core.data.network.retry

/** Observer of retry-loop lifecycle events, e.g. for analytics. All methods default to no-ops. */
interface RetryEventListener {
    /** A retry was scheduled after a failed attempt and will run after [RetryAttempt.delay] ms. */
    suspend fun onRetryScheduled(
        attempt: RetryAttempt,
        maxAttempts: Int,
    ) = Unit

    /** The request eventually succeeded after [retries] retries. */
    suspend fun onRecovered(
        retries: Int,
        statusCode: Int,
    ) = Unit

    /**
     * The retry loop gave up; [history] holds every recorded retry decision, including a final
     * one whose delay never elapsed when the total-timeout cap aborted the loop.
     */
    suspend fun onExhausted(history: List<RetryAttempt>) = Unit
}
