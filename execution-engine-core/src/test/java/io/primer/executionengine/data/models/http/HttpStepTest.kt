package io.primer.executionengine.data.models.http

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class HttpStepTest {

    @Test
    fun `deserialize should parse minimal step with defaults`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com/data")
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertEquals("http", result.type)
        assertEquals("https://api.example.com/data", result.url)
        assertEquals(HttpMethod.GET, result.method)
        assertNull(result.body)
        assertNull(result.headers)
        assertNull(result.query)
        assertNull(result.retry)
        assertNull(result.timeout)
        assertNull(result.state)
    }

    @Test
    fun `deserialize should parse POST method`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com/submit")
            put("method", "POST")
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertEquals(HttpMethod.POST, result.method)
    }

    @Test
    fun `deserialize should parse method case-insensitively`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com")
            put("method", "post")
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertEquals(HttpMethod.POST, result.method)
    }

    @Test
    fun `deserialize should parse all HTTP methods`() {
        for (method in HttpMethod.entries) {
            val json = JSONObject().apply {
                put("type", "http")
                put("url", "https://api.example.com")
                put("method", method.name)
            }

            val result = HttpStep.deserializer.deserialize(json)

            assertEquals(method, result.method)
        }
    }

    @Test
    fun `deserialize should parse body`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com")
            put("body", JSONObject().put("name", "test").put("count", 42))
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertEquals("test", result.body?.get("name"))
        assertEquals(42, result.body?.get("count"))
    }

    @Test
    fun `deserialize should parse headers`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com")
            put(
                "headers",
                JSONObject()
                    .put("Authorization", "Bearer token")
                    .put("Content-Type", "application/json"),
            )
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertEquals("Bearer token", result.headers?.get("Authorization"))
        assertEquals("application/json", result.headers?.get("Content-Type"))
    }

    @Test
    fun `deserialize should parse query parameters`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com/search")
            put("query", JSONObject().put("q", "test").put("page", "1"))
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertEquals("test", result.query?.get("q"))
        assertEquals("1", result.query?.get("page"))
    }

    @Test
    fun `deserialize should parse timeout`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com")
            put("timeout", 5000)
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertEquals(5000, result.timeout)
    }

    @Test
    fun `deserialize should parse null timeout`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com")
            put("timeout", JSONObject.NULL)
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertNull(result.timeout)
    }

    @Test
    fun `deserialize should parse retry config`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com")
            put(
                "retry",
                JSONObject().apply {
                    put("maxAttempts", 3)
                    put("backoff", "exponential")
                    put("retryOn", JSONArray(listOf(500, 502, 503)))
                    put("delay", 1000)
                },
            )
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertEquals(3, result.retry?.maxAttempts)
        assertEquals(BackoffStrategy.EXPONENTIAL, result.retry?.backoff)
        assertEquals(listOf(500, 502, 503), result.retry?.retryOn)
        assertEquals(1000L, result.retry?.delay)
    }

    @Test
    fun `deserialize should parse state`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com")
            put("state", JSONObject().put("key", "value"))
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertEquals("value", result.state?.get("key"))
    }

    @Test
    fun `deserialize should parse all fields together`() {
        val json = JSONObject().apply {
            put("type", "http")
            put("url", "https://api.example.com/submit")
            put("method", "PUT")
            put("body", JSONObject().put("data", "payload"))
            put("headers", JSONObject().put("X-Custom", "header"))
            put("query", JSONObject().put("filter", "active"))
            put("timeout", 10000)
            put("state", JSONObject().put("step", "1"))
        }

        val result = HttpStep.deserializer.deserialize(json)

        assertEquals("http", result.type)
        assertEquals("https://api.example.com/submit", result.url)
        assertEquals(HttpMethod.PUT, result.method)
        assertEquals("payload", result.body?.get("data"))
        assertEquals("header", result.headers?.get("X-Custom"))
        assertEquals("active", result.query?.get("filter"))
        assertEquals(10000, result.timeout)
        assertEquals("1", result.state?.get("step"))
    }

    @Test
    fun `deserialize should throw when type is missing`() {
        val json = JSONObject().apply {
            put("url", "https://api.example.com")
        }

        assertThrows(Exception::class.java) {
            HttpStep.deserializer.deserialize(json)
        }
    }

    @Test
    fun `deserialize should throw when url is missing`() {
        val json = JSONObject().apply {
            put("type", "http")
        }

        assertThrows(Exception::class.java) {
            HttpStep.deserializer.deserialize(json)
        }
    }
}
