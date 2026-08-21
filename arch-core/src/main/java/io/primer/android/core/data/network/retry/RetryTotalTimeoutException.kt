package io.primer.android.core.data.network.retry

import java.io.IOException

/**
 * Thrown (with 408 semantics) when the retry loop would exceed [totalTimeoutMs] without the
 * request ever settling; [cause] is the last attempt's error.
 */
class RetryTotalTimeoutException(
    val totalTimeoutMs: Long,
    cause: Throwable? = null,
) : IOException("Retry total timeout exceeded after ${totalTimeoutMs}ms.", cause)
