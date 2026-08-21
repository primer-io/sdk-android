package io.primer.android.core.data.network.retry

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RetryDelaysTest {
    private fun policy(backoff: RetryBackoff) =
        ResolvedRetryPolicy(
            maxAttempts = 9,
            backoff = backoff,
            baseDelayMs = 100L,
            retryOn = null,
            totalTimeoutMs = 300_000L,
        )

    @Test
    fun `jittered delay is half the window when random returns zero`() {
        assertEquals(50L, jitteredDelayMs(windowMs = 100L, random = { 0.0 }))
    }

    @Test
    fun `jittered delay is the full window when random returns one`() {
        assertEquals(100L, jitteredDelayMs(windowMs = 100L, random = { 1.0 }))
    }

    @Test
    fun `jittered delay rounds the result`() {
        assertEquals(51L, jitteredDelayMs(windowMs = 101L, random = { 0.0 }))
    }

    @Test
    fun `exponential backoff doubles the window per failed attempt`() {
        val policy = policy(RetryBackoff.EXPONENTIAL)

        assertEquals(100L, backoffDelayMs(policy = policy, failedAttempts = 1, random = { 1.0 }))
        assertEquals(200L, backoffDelayMs(policy = policy, failedAttempts = 2, random = { 1.0 }))
        assertEquals(400L, backoffDelayMs(policy = policy, failedAttempts = 3, random = { 1.0 }))
        assertEquals(800L, backoffDelayMs(policy = policy, failedAttempts = 4, random = { 1.0 }))
    }

    @Test
    fun `fixed backoff keeps the window flat`() {
        val policy = policy(RetryBackoff.FIXED)

        assertEquals(100L, backoffDelayMs(policy = policy, failedAttempts = 1, random = { 1.0 }))
        assertEquals(100L, backoffDelayMs(policy = policy, failedAttempts = 5, random = { 1.0 }))
        assertEquals(100L, backoffDelayMs(policy = policy, failedAttempts = 9, random = { 1.0 }))
    }

    @Test
    fun `exponential window caps at thirty seconds`() {
        val policy = policy(RetryBackoff.EXPONENTIAL)

        assertEquals(30_000L, backoffDelayMs(policy = policy, failedAttempts = 20, random = { 1.0 }))
        assertEquals(15_000L, backoffDelayMs(policy = policy, failedAttempts = 20, random = { 0.0 }))
    }

    @Test
    fun `backoff delay applies jitter to the window`() {
        val policy = policy(RetryBackoff.EXPONENTIAL)

        assertEquals(100L, backoffDelayMs(policy = policy, failedAttempts = 2, random = { 0.0 }))
        assertEquals(150L, backoffDelayMs(policy = policy, failedAttempts = 2, random = { 0.5 }))
    }

    @Test
    fun `unknown backoff falls back to the exponential window`() {
        val policy = policy(RetryBackoff.UNKNOWN)

        assertEquals(100L, backoffDelayMs(policy = policy, failedAttempts = 1, random = { 1.0 }))
        assertEquals(200L, backoffDelayMs(policy = policy, failedAttempts = 2, random = { 1.0 }))
    }

    @Test
    fun `safeValueOf resolves known values case-insensitively and defaults to UNKNOWN`() {
        assertEquals(RetryBackoff.EXPONENTIAL, RetryBackoff.safeValueOf("exponential"))
        assertEquals(RetryBackoff.FIXED, RetryBackoff.safeValueOf("FIXED"))
        assertEquals(RetryBackoff.UNKNOWN, RetryBackoff.safeValueOf("linear"))
        assertEquals(RetryBackoff.UNKNOWN, RetryBackoff.safeValueOf(null))
    }
}
