package io.primer.android.core.data.network.retry

private const val SERVER_ERROR_STATUS_CODE_FIRST = 500
private const val SERVER_ERROR_STATUS_CODE_LAST = 599

private val SERVER_ERROR_STATUS_CODES = SERVER_ERROR_STATUS_CODE_FIRST..SERVER_ERROR_STATUS_CODE_LAST

/**
 * The built-in retry strategy backing a [ResolvedRetryPolicy]:
 * - stops once [ResolvedRetryPolicy.maxAttempts] attempts have failed;
 * - a throwing attempt always gets another chance (until max attempts);
 * - otherwise retries only [retryable][isRetryableFailure] failures, with a jittered backoff delay.
 */
fun ResolvedRetryPolicy.toRetryStrategy(random: () -> Double = Math::random): RetryStrategy =
    RetryStrategy { context ->
        when {
            context.attempt >= maxAttempts -> RetryDecision.Stop
            context.threw || isRetryableFailure(context.response) ->
                RetryDecision.RetryAfter(
                    delayMs = backoffDelayMs(policy = this, failedAttempts = context.attempt, random = random),
                )

            else -> RetryDecision.Stop
        }
    }

/**
 * A settled failure is retryable when its status is in [ResolvedRetryPolicy.retryOn]; a `null`
 * [ResolvedRetryPolicy.retryOn] means every 5xx (an explicit list replaces the 5xx default entirely).
 * A never-settled failure is retryable only when [transient][isTransientFailure].
 */
fun ResolvedRetryPolicy.isRetryableFailure(failure: HttpAttemptFailure): Boolean =
    when (failure) {
        is HttpAttemptFailure.SettledFailure ->
            retryOn?.contains(failure.response.statusCode)
                ?: (failure.response.statusCode in SERVER_ERROR_STATUS_CODES)

        is HttpAttemptFailure.TransportFailure -> isTransientFailure(failure.error)
    }
