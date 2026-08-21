package io.primer.executionengine.data.models.http

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class HttpRequestParamsTest {

    @Test
    fun `deserializer should parse every field including the timeoutMs wire name`() {
        val json = JSONObject(
            """
            {
                "method": "POST",
                "url": "https://example.com/checkout",
                "body": {"a": 1},
                "timeoutMs": 5000,
                "retry": {"maxAttempts": 2},
                "idempotencyKey": "key-123"
            }
            """.trimIndent(),
        )

        val params = HttpRequestParams.deserializer.deserialize(json)

        assertEquals("POST", params.method)
        assertEquals("https://example.com/checkout", params.url)
        assertEquals("""{"a":1}""", params.body.toString())
        assertEquals(5000L, params.timeoutMs)
        assertEquals(
            HttpRetryParams(maxAttempts = 2, backoff = null, baseDelayMs = null, retryOn = null),
            params.retry,
        )
        assertEquals("key-123", params.idempotencyKey)
    }

    @Test
    fun `deserializer should keep raw scalar body values`() {
        val stringBody = HttpRequestParams.deserializer.deserialize(
            JSONObject("""{"method":"POST","url":"u","body":"text"}"""),
        )
        val numberBody = HttpRequestParams.deserializer.deserialize(
            JSONObject("""{"method":"POST","url":"u","body":42}"""),
        )
        val booleanBody = HttpRequestParams.deserializer.deserialize(
            JSONObject("""{"method":"POST","url":"u","body":true}"""),
        )

        assertEquals("text", stringBody.body)
        assertEquals(42, numberBody.body)
        assertEquals(true, booleanBody.body)
    }

    @Test
    fun `deserializer should map an explicit JSON null body to null`() {
        val json = JSONObject("""{"method":"GET","url":"u","body":null}""")

        val params = HttpRequestParams.deserializer.deserialize(json)

        assertNull(params.body)
    }

    @Test
    fun `deserializer should default absent optional fields to null`() {
        val json = JSONObject("""{"method":"GET","url":"u"}""")

        val params = HttpRequestParams.deserializer.deserialize(json)

        assertNull(params.body)
        assertNull(params.timeoutMs)
        assertNull(params.retry)
        assertNull(params.idempotencyKey)
    }

    @Test
    fun `deserializer should ignore unknown extra fields`() {
        val json = JSONObject("""{"method":"GET","url":"u","somethingNew":{"x":1},"other":"y"}""")

        val params = HttpRequestParams.deserializer.deserialize(json)

        assertEquals("GET", params.method)
        assertEquals("u", params.url)
    }
}
