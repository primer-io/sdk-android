package io.primer.android.core.data.network.retry

import io.primer.android.core.data.network.transport.RawHttpResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HttpRetryLoopTest {
    private class RecordingRetryListener : RetryEventListener {
        val scheduled = mutableListOf<RetryAttempt>()
        var recovered: Pair<Int, Int>? = null
        var exhausted: List<RetryAttempt>? = null

        override suspend fun onRetryScheduled(attempt: RetryAttempt, maxAttempts: Int) {
            scheduled += attempt
        }

        override suspend fun onRecovered(retries: Int, statusCode: Int) {
            recovered = retries to statusCode
        }

        override suspend fun onExhausted(history: List<RetryAttempt>) {
            exhausted = history
        }
    }

    private val listener = RecordingRetryListener()

    private fun response(statusCode: Int) =
        RawHttpResponse(
            requestId = "request-id",
            statusCode = statusCode,
            headers = emptyMap(),
            bodyText = null,
        )

    private fun policy(
        maxAttempts: Int = 9,
        backoff: RetryBackoff = RetryBackoff.EXPONENTIAL,
        baseDelayMs: Long = 100L,
        retryOn: List<Int>? = null,
        totalTimeoutMs: Long = 300_000L,
    ) = ResolvedRetryPolicy(
        maxAttempts = maxAttempts,
        backoff = backoff,
        baseDelayMs = baseDelayMs,
        retryOn = retryOn,
        totalTimeoutMs = totalTimeoutMs,
    )

    private fun TestScope.retryLoop(random: () -> Double = { 1.0 }) =
        HttpRetryLoop(currentTimeMs = { testScheduler.currentTime }, random = random, listener = listener)

    @Test
    fun `a first-attempt success is returned settled with no retries reported`() =
        runTest {
            var attempts = 0

            val result =
                retryLoop().execute(policy = policy()) {
                    attempts++
                    HttpAttemptResult.Response(response(200))
                }

            assertEquals(HttpTransportResult.Settled(response(200)), result)
            assertEquals(1, attempts)
            assertEquals(emptyList(), listener.scheduled)
            assertNull(listener.recovered)
        }

    @Test
    fun `the default policy runs nine attempts total then returns the last settled failure`() =
        runTest {
            var attempts = 0

            val result =
                retryLoop().execute(policy = (null as RetryPolicy?).resolve()) {
                    attempts++
                    HttpAttemptResult.Response(response(500))
                }

            assertEquals(9, attempts)
            val settled = assertIs<HttpTransportResult.Settled>(result)
            assertEquals(500, settled.response.statusCode)
            assertEquals(8, listener.scheduled.size)
        }

    @Test
    fun `a non-retryable settled status stops after one attempt and is returned settled`() =
        runTest {
            var attempts = 0

            val result =
                retryLoop().execute(policy = policy()) {
                    attempts++
                    HttpAttemptResult.Response(response(404))
                }

            assertEquals(1, attempts)
            assertEquals(HttpTransportResult.Settled(response(404)), result)
        }

    @Test
    fun `a throwing attempt is retried with threw true in the strategy context and can recover`() =
        runTest {
            var attempts = 0
            val boom = RuntimeException("boom")
            val contexts = mutableListOf<RetryContext>()
            val recordingStrategy =
                RetryStrategy { context ->
                    contexts += context
                    RetryDecision.RetryAfter(delayMs = 10L)
                }

            val result =
                retryLoop().execute(policy = policy(), strategy = recordingStrategy) {
                    attempts++
                    if (attempts == 1) throw boom
                    HttpAttemptResult.Response(response(200))
                }

            assertEquals(2, attempts)
            assertEquals(1, contexts.size)
            assertEquals(true, contexts.first().threw)
            assertEquals(1, contexts.first().attempt)
            assertEquals(HttpAttemptFailure.TransportFailure(boom), contexts.first().response)
            val settled = assertIs<HttpTransportResult.Settled>(result)
            assertEquals(200, settled.response.statusCode)
            assertEquals(
                listOf<RetryAttemptError>(RetryAttemptError.Thrown(boom)),
                listener.scheduled.map { it.error },
            )
            assertEquals(1 to 200, listener.recovered)
        }

    @Test
    fun `total timeout returns failed with RetryTotalTimeoutException and reports the exhausted retries`() =
        runTest {
            var attempts = 0
            val ioError = IOException("connection reset")

            val result =
                retryLoop().execute(
                    policy =
                    policy(
                        maxAttempts = 10,
                        backoff = RetryBackoff.FIXED,
                        baseDelayMs = 100L,
                        totalTimeoutMs = 250L,
                    ),
                ) {
                    attempts++
                    HttpAttemptResult.TransportError(ioError)
                }

            assertEquals(3, attempts)
            val failed = assertIs<HttpTransportResult.Failed>(result)
            val error = assertIs<RetryTotalTimeoutException>(failed.error)
            assertEquals(250L, error.totalTimeoutMs)
            assertEquals(ioError, error.cause)
            assertEquals(3, listener.exhausted?.size)
        }

    @Test
    fun `total timeout returns the last settled failure and reports the exhausted retries`() =
        runTest {
            var attempts = 0

            val result =
                retryLoop().execute(
                    policy =
                    policy(
                        maxAttempts = 10,
                        backoff = RetryBackoff.FIXED,
                        baseDelayMs = 100L,
                        totalTimeoutMs = 250L,
                    ),
                ) {
                    attempts++
                    HttpAttemptResult.Response(response(503))
                }

            assertEquals(3, attempts)
            val settled = assertIs<HttpTransportResult.Settled>(result)
            assertEquals(503, settled.response.statusCode)
            assertEquals(3, listener.exhausted?.size)
        }

    @Test
    fun `cancellation during the between-attempt delay rethrows and does not retry`() =
        runTest {
            var attempts = 0
            var caught: Throwable? = null
            val loop = retryLoop()

            val job =
                launch {
                    try {
                        loop.execute(policy = policy(backoff = RetryBackoff.FIXED, baseDelayMs = 10_000L)) {
                            attempts++
                            HttpAttemptResult.Response(response(500))
                        }
                    } catch (expected: CancellationException) {
                        caught = expected
                        throw expected
                    }
                }
            runCurrent()
            assertEquals(1, attempts)

            job.cancel()
            advanceUntilIdle()

            assertEquals(1, attempts)
            assertTrue(job.isCancelled)
            assertIs<CancellationException>(assertNotNull(caught))
        }

    @Test
    fun `a custom strategy wins over the policy and its delays are reported to the listener`() =
        runTest {
            var attempts = 0
            val customStrategy =
                RetryStrategy { context ->
                    if (context.attempt == 1) RetryDecision.RetryAfter(delayMs = 123L) else RetryDecision.Stop
                }

            val result =
                retryLoop().execute(policy = policy(maxAttempts = 1), strategy = customStrategy) {
                    attempts++
                    HttpAttemptResult.Response(response(500))
                }

            assertEquals(2, attempts)
            val settled = assertIs<HttpTransportResult.Settled>(result)
            assertEquals(500, settled.response.statusCode)
            assertEquals(listOf(123L), listener.scheduled.map { it.delay })
        }

    @Test
    fun `a runaway custom strategy is still capped by the total timeout`() =
        runTest {
            var attempts = 0
            val runawayStrategy = RetryStrategy { RetryDecision.RetryAfter(delayMs = 50L) }

            val result =
                retryLoop().execute(
                    policy = policy(maxAttempts = 1, totalTimeoutMs = 120L),
                    strategy = runawayStrategy,
                ) {
                    attempts++
                    HttpAttemptResult.Response(response(500))
                }

            assertEquals(3, attempts)
            val settled = assertIs<HttpTransportResult.Settled>(result)
            assertEquals(500, settled.response.statusCode)
            assertEquals(3, listener.exhausted?.size)
        }

    @Test
    fun `stop returns the settled failure and reports the accumulated retries`() =
        runTest {
            var attempts = 0

            val result =
                retryLoop().execute(
                    policy =
                    policy(
                        maxAttempts = 3,
                        backoff = RetryBackoff.FIXED,
                        baseDelayMs = 100L,
                        retryOn = listOf(500),
                    ),
                ) {
                    attempts++
                    HttpAttemptResult.Response(response(500))
                }

            assertEquals(3, attempts)
            val settled = assertIs<HttpTransportResult.Settled>(result)
            assertEquals(500, settled.response.statusCode)
            assertEquals(listOf(100L, 100L), listener.scheduled.map { it.delay })
            assertEquals(2, listener.exhausted?.size)
        }

    @Test
    fun `a negative strategy delay is clamped to zero`() =
        runTest {
            var attempts = 0
            val negativeDelayStrategy =
                RetryStrategy { context ->
                    if (context.attempt == 1) RetryDecision.RetryAfter(delayMs = -5L) else RetryDecision.Stop
                }

            val result =
                retryLoop().execute(policy = policy(), strategy = negativeDelayStrategy) {
                    attempts++
                    HttpAttemptResult.Response(response(500))
                }

            assertEquals(2, attempts)
            assertIs<HttpTransportResult.Settled>(result)
            assertEquals(listOf(0L), listener.scheduled.map { it.delay })
        }
}
