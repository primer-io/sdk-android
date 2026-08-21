package io.primer.android.core.data.network.retry

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RetryPolicyResolutionTest {
    @Test
    fun `resolving a null policy falls back to the Android transport defaults`() {
        val resolved = (null as RetryPolicy?).resolve()

        assertEquals(9, resolved.maxAttempts)
        assertEquals(RetryBackoff.EXPONENTIAL, resolved.backoff)
        assertEquals(100L, resolved.baseDelayMs)
        assertNull(resolved.retryOn)
        assertEquals(300_000L, resolved.totalTimeoutMs)
    }

    @Test
    fun `null fields fall back to the defaults field by field`() {
        val resolved = RetryPolicy(maxAttempts = 3).resolve()

        assertEquals(3, resolved.maxAttempts)
        assertEquals(RetryBackoff.EXPONENTIAL, resolved.backoff)
        assertEquals(100L, resolved.baseDelayMs)
        assertNull(resolved.retryOn)
        assertEquals(300_000L, resolved.totalTimeoutMs)
    }

    @Test
    fun `maxAttempts of zero is clamped to one`() {
        assertEquals(1, RetryPolicy(maxAttempts = 0).resolve().maxAttempts)
    }

    @Test
    fun `negative maxAttempts is clamped to one`() {
        assertEquals(1, RetryPolicy(maxAttempts = -1).resolve().maxAttempts)
    }

    @Test
    fun `retryOn stays null when absent`() {
        assertNull(RetryPolicy().resolve().retryOn)
    }

    @Test
    fun `explicit values pass through unchanged`() {
        val resolved =
            RetryPolicy(
                maxAttempts = 5,
                backoff = RetryBackoff.FIXED,
                baseDelayMs = 50L,
                retryOn = listOf(500, 502),
                totalTimeoutMs = 1_000L,
            ).resolve()

        assertEquals(
            ResolvedRetryPolicy(
                maxAttempts = 5,
                backoff = RetryBackoff.FIXED,
                baseDelayMs = 50L,
                retryOn = listOf(500, 502),
                totalTimeoutMs = 1_000L,
            ),
            resolved,
        )
    }

    @Test
    fun `an explicit empty retryOn passes through unchanged`() {
        assertEquals(emptyList(), RetryPolicy(retryOn = emptyList()).resolve().retryOn)
    }
}
