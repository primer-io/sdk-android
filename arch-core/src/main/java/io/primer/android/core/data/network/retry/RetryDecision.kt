package io.primer.android.core.data.network.retry

sealed interface RetryDecision {
    data class RetryAfter(val delayMs: Long) : RetryDecision

    data object Stop : RetryDecision
}
