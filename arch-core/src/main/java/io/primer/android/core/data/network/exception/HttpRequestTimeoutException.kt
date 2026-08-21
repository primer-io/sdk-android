package io.primer.android.core.data.network.exception

import java.io.IOException

/**
 * The per-attempt timeout stamp: raised by the transport when an attempt exceeds its timeout.
 * Extends [IOException] so it is classified as a transient (retryable) failure.
 */
class HttpRequestTimeoutException(
    val timeoutMs: Long?,
    cause: Throwable? = null,
) : IOException("HTTP request timed out after ${timeoutMs}ms.", cause)
