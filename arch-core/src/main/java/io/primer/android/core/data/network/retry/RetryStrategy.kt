package io.primer.android.core.data.network.retry

fun interface RetryStrategy {
    fun decide(context: RetryContext): RetryDecision
}
