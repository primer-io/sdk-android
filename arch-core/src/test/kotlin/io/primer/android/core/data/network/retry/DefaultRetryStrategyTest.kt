package io.primer.android.core.data.network.retry

import io.primer.android.core.data.network.transport.RawHttpResponse
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.test.assertEquals

class DefaultRetryStrategyTest {
    private fun policy(
        maxAttempts: Int = 9,
        retryOn: List<Int>? = null,
    ) = ResolvedRetryPolicy(
        maxAttempts = maxAttempts,
        backoff = RetryBackoff.FIXED,
        baseDelayMs = 100L,
        retryOn = retryOn,
        totalTimeoutMs = 300_000L,
    )

    private fun settled(statusCode: Int) =
        HttpAttemptFailure.SettledFailure(
            response =
            RawHttpResponse(
                requestId = "request-id",
                statusCode = statusCode,
                headers = emptyMap(),
                bodyText = null,
            ),
        )

    private fun context(
        attempt: Int = 1,
        failure: HttpAttemptFailure,
        threw: Boolean = false,
    ) = RetryContext(attempt = attempt, response = failure, elapsedMs = 0L, threw = threw)

    // With FIXED backoff of 100ms and random 0.0 the built-in delay is always 50ms.
    private val retryAfterFixedDelay = RetryDecision.RetryAfter(delayMs = 50L)

    @Test
    fun `an explicit retryOn list replaces the five-xx default entirely`() {
        val strategy = policy(retryOn = listOf(502)).toRetryStrategy(random = { 0.0 })

        assertEquals(RetryDecision.Stop, strategy.decide(context(failure = settled(500))))
        assertEquals(retryAfterFixedDelay, strategy.decide(context(failure = settled(502))))
    }

    @Test
    fun `an empty retryOn retries no status`() {
        val strategy = policy(retryOn = emptyList()).toRetryStrategy(random = { 0.0 })

        assertEquals(RetryDecision.Stop, strategy.decide(context(failure = settled(500))))
        assertEquals(RetryDecision.Stop, strategy.decide(context(failure = settled(502))))
    }

    @Test
    fun `a null retryOn retries every five-xx and stops on four-xx`() {
        val strategy = policy(retryOn = null).toRetryStrategy(random = { 0.0 })

        assertEquals(retryAfterFixedDelay, strategy.decide(context(failure = settled(500))))
        assertEquals(retryAfterFixedDelay, strategy.decide(context(failure = settled(599))))
        assertEquals(RetryDecision.Stop, strategy.decide(context(failure = settled(404))))
        assertEquals(RetryDecision.Stop, strategy.decide(context(failure = settled(499))))
    }

    @Test
    fun `a throwing attempt is retried regardless of failure classification`() {
        val strategy = policy(retryOn = emptyList()).toRetryStrategy(random = { 0.0 })

        assertEquals(
            retryAfterFixedDelay,
            strategy.decide(context(failure = settled(400), threw = true)),
        )
        assertEquals(
            retryAfterFixedDelay,
            strategy.decide(
                context(
                    failure = HttpAttemptFailure.TransportFailure(IllegalStateException()),
                    threw = true,
                ),
            ),
        )
    }

    @Test
    fun `a throwing attempt is retried only until max attempts`() {
        val strategy = policy(maxAttempts = 3).toRetryStrategy(random = { 0.0 })

        assertEquals(
            retryAfterFixedDelay,
            strategy.decide(context(attempt = 2, failure = settled(400), threw = true)),
        )
        assertEquals(
            RetryDecision.Stop,
            strategy.decide(context(attempt = 3, failure = settled(400), threw = true)),
        )
    }

    @Test
    fun `a transport failure is judged by transience`() {
        val strategy = policy().toRetryStrategy(random = { 0.0 })

        assertEquals(
            retryAfterFixedDelay,
            strategy.decide(context(failure = HttpAttemptFailure.TransportFailure(IOException()))),
        )
        assertEquals(
            RetryDecision.Stop,
            strategy.decide(context(failure = HttpAttemptFailure.TransportFailure(IllegalStateException()))),
        )
    }

    @Test
    fun `stops once max attempts have failed even for retryable failures`() {
        val strategy = policy(maxAttempts = 3).toRetryStrategy(random = { 0.0 })

        assertEquals(retryAfterFixedDelay, strategy.decide(context(attempt = 2, failure = settled(500))))
        assertEquals(RetryDecision.Stop, strategy.decide(context(attempt = 3, failure = settled(500))))
        assertEquals(RetryDecision.Stop, strategy.decide(context(attempt = 4, failure = settled(500))))
    }
}
