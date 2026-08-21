package io.primer.android.core.data.network.retry

import io.primer.android.core.data.network.transport.RawHttpResponse
import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException

/** The transport-level outcome of a single HTTP attempt. */
sealed interface HttpAttemptResult {
    data class Response(val response: RawHttpResponse) : HttpAttemptResult

    data class TransportError(val error: Throwable) : HttpAttemptResult
}

/** The final outcome of a (possibly retried) HTTP exchange. */
sealed interface HttpTransportResult {
    /** The request settled with an HTTP response of any status. */
    data class Settled(val response: RawHttpResponse) : HttpTransportResult

    /** The request never settled; [error] is the last (or wrapping) failure. */
    data class Failed(val error: Throwable) : HttpTransportResult
}

/**
 * Runs HTTP attempts under a [RetryStrategy], applying the [ResolvedRetryPolicy.totalTimeoutMs]
 * cap and recording every scheduled retry. Cancellation is never retried: a [CancellationException]
 * thrown by an attempt (or during a between-attempt delay) is rethrown immediately.
 */
class HttpRetryLoop(
    private val currentTimeMs: () -> Long = System::currentTimeMillis,
    private val random: () -> Double = Math::random,
    private val listener: RetryEventListener? = null,
) {
    suspend fun execute(
        policy: ResolvedRetryPolicy,
        strategy: RetryStrategy? = null,
        attempt: suspend () -> HttpAttemptResult,
    ): HttpTransportResult {
        val effectiveStrategy = strategy ?: policy.toRetryStrategy(random)
        val history = mutableListOf<RetryAttempt>()
        val startMs = currentTimeMs()
        var failedAttempts = 0
        var result: HttpTransportResult? = null

        while (result == null) {
            result =
                when (val outcome = runAttempt(attempt)) {
                    is AttemptOutcome.Success -> settledSuccess(outcome.response, history)
                    is AttemptOutcome.Failure -> {
                        failedAttempts++
                        val context =
                            RetryContext(
                                attempt = failedAttempts,
                                response = outcome.failure,
                                elapsedMs = currentTimeMs() - startMs,
                                threw = outcome.threw,
                            )
                        decideOutcome(policy, effectiveStrategy, outcome, context, history)
                    }
                }
        }
        return result
    }

    /** Applies the strategy decision; returns `null` when another attempt should run after the delay. */
    private suspend fun decideOutcome(
        policy: ResolvedRetryPolicy,
        strategy: RetryStrategy,
        failure: AttemptOutcome.Failure,
        context: RetryContext,
        history: MutableList<RetryAttempt>,
    ): HttpTransportResult? =
        when (val decision = strategy.decide(context)) {
            is RetryDecision.Stop -> stopOutcome(failure, history)
            is RetryDecision.RetryAfter -> {
                val delayMs = decision.delayMs.coerceAtLeast(0L)
                val retryAttempt =
                    RetryAttempt(
                        attempt = context.attempt,
                        delay = delayMs,
                        error = failure.toRetryAttemptError(),
                        timestamp = currentTimeMs(),
                    )
                history += retryAttempt
                if (context.elapsedMs + delayMs > policy.totalTimeoutMs) {
                    totalTimeoutOutcome(policy, failure, history)
                } else {
                    listener?.onRetryScheduled(attempt = retryAttempt, maxAttempts = policy.maxAttempts)
                    delay(delayMs)
                    null
                }
            }
        }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun runAttempt(attempt: suspend () -> HttpAttemptResult): AttemptOutcome =
        try {
            when (val result = attempt()) {
                is HttpAttemptResult.Response ->
                    if (result.response.isSuccess) {
                        AttemptOutcome.Success(result.response)
                    } else {
                        AttemptOutcome.Failure(
                            failure = HttpAttemptFailure.SettledFailure(result.response),
                            threw = false,
                        )
                    }

                is HttpAttemptResult.TransportError ->
                    AttemptOutcome.Failure(
                        failure = HttpAttemptFailure.TransportFailure(result.error),
                        threw = false,
                    )
            }
        } catch (expected: CancellationException) {
            throw expected
        } catch (expected: Throwable) {
            AttemptOutcome.Failure(failure = HttpAttemptFailure.TransportFailure(expected), threw = true)
        }

    private suspend fun settledSuccess(
        response: RawHttpResponse,
        history: List<RetryAttempt>,
    ): HttpTransportResult {
        if (history.isNotEmpty()) {
            listener?.onRecovered(retries = history.size, statusCode = response.statusCode)
        }
        return HttpTransportResult.Settled(response)
    }

    private suspend fun stopOutcome(
        failure: AttemptOutcome.Failure,
        history: List<RetryAttempt>,
    ): HttpTransportResult {
        listener?.onExhausted(history.toList())
        return when (val attemptFailure = failure.failure) {
            is HttpAttemptFailure.SettledFailure -> HttpTransportResult.Settled(attemptFailure.response)
            is HttpAttemptFailure.TransportFailure -> HttpTransportResult.Failed(attemptFailure.error)
        }
    }

    private suspend fun totalTimeoutOutcome(
        policy: ResolvedRetryPolicy,
        failure: AttemptOutcome.Failure,
        history: List<RetryAttempt>,
    ): HttpTransportResult {
        listener?.onExhausted(history.toList())
        return when (val attemptFailure = failure.failure) {
            is HttpAttemptFailure.SettledFailure -> HttpTransportResult.Settled(attemptFailure.response)
            is HttpAttemptFailure.TransportFailure ->
                HttpTransportResult.Failed(
                    RetryTotalTimeoutException(
                        totalTimeoutMs = policy.totalTimeoutMs,
                        cause = attemptFailure.error,
                    ),
                )
        }
    }

    private fun AttemptOutcome.Failure.toRetryAttemptError(): RetryAttemptError =
        when (val attemptFailure = failure) {
            is HttpAttemptFailure.SettledFailure -> RetryAttemptError.Status(attemptFailure.response.statusCode)
            is HttpAttemptFailure.TransportFailure -> RetryAttemptError.Thrown(attemptFailure.error)
        }

    private sealed interface AttemptOutcome {
        data class Success(val response: RawHttpResponse) : AttemptOutcome

        data class Failure(val failure: HttpAttemptFailure, val threw: Boolean) : AttemptOutcome
    }
}
