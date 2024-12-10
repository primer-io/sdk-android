package io.primer.android.core.data.network.retry

data class RetryConfig(
    val enabled: Boolean,
    var retries: Int = 0,
    val maxRetries: Int = MAX_RETRIES,
    val initialBackoff: Int = INITIAL_BACKOFF,
    val retryNetworkErrors: Boolean = true,
    val retry500Errors: Boolean = false,
    val maxJitter: Int = MAX_JITTER
)
