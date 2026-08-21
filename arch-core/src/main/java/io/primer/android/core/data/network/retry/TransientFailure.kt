package io.primer.android.core.data.network.retry

import java.io.IOException

/**
 * A never-settled failure is retryable only when transient: the whole [IOException] family,
 * including [io.primer.android.core.data.network.exception.HttpRequestTimeoutException]
 * (which extends [IOException]). Cancellation never reaches classification - it is rethrown
 * by the retry loop before any retry decision is made.
 */
fun isTransientFailure(error: Throwable): Boolean = error is IOException
