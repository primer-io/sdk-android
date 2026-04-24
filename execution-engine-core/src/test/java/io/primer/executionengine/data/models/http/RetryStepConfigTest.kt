package io.primer.executionengine.data.models.http

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class RetryStepConfigTest {

    @Test
    fun `deserialize should parse all fields`() {
        val json = JSONObject().apply {
            put("maxAttempts", 5)
            put("backoff", "exponential")
            put("retryOn", JSONArray(listOf(500, 502, 503)))
            put("delay", 2000)
        }

        val result = RetryStepConfig.deserializer.deserialize(json)

        assertEquals(5, result.maxAttempts)
        assertEquals(BackoffStrategy.EXPONENTIAL, result.backoff)
        assertEquals(listOf(500, 502, 503), result.retryOn)
        assertEquals(2000L, result.delay)
    }

    @Test
    fun `deserialize should use defaults for optional fields`() {
        val json = JSONObject().apply {
            put("maxAttempts", 3)
        }

        val result = RetryStepConfig.deserializer.deserialize(json)

        assertEquals(3, result.maxAttempts)
        assertEquals(BackoffStrategy.CONSTANT, result.backoff)
        assertEquals(emptyList<Int>(), result.retryOn)
        assertEquals(0L, result.delay)
    }

    @Test
    fun `deserialize should parse linear backoff strategy`() {
        val json = JSONObject().apply {
            put("maxAttempts", 2)
            put("backoff", "linear")
        }

        val result = RetryStepConfig.deserializer.deserialize(json)

        assertEquals(BackoffStrategy.LINEAR, result.backoff)
    }

    @Test
    fun `deserialize should parse backoff case-insensitively`() {
        val json = JSONObject().apply {
            put("maxAttempts", 2)
            put("backoff", "Exponential")
        }

        val result = RetryStepConfig.deserializer.deserialize(json)

        assertEquals(BackoffStrategy.EXPONENTIAL, result.backoff)
    }

    @Test
    fun `deserialize should handle empty retryOn array`() {
        val json = JSONObject().apply {
            put("maxAttempts", 1)
            put("retryOn", JSONArray())
        }

        val result = RetryStepConfig.deserializer.deserialize(json)

        assertEquals(emptyList<Int>(), result.retryOn)
    }

    @Test
    fun `deserialize should parse single retryOn status code`() {
        val json = JSONObject().apply {
            put("maxAttempts", 1)
            put("retryOn", JSONArray(listOf(429)))
        }

        val result = RetryStepConfig.deserializer.deserialize(json)

        assertEquals(listOf(429), result.retryOn)
    }

    @Test
    fun `deserialize should throw when maxAttempts is missing`() {
        val json = JSONObject().apply {
            put("backoff", "constant")
        }

        assertThrows(Exception::class.java) {
            RetryStepConfig.deserializer.deserialize(json)
        }
    }
}
