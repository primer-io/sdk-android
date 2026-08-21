package io.primer.executionengine.data.models.http

import io.primer.android.core.data.network.retry.RetryBackoff
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class HttpRetryParamsTest {

    @Test
    fun `deserializer should parse every field`() {
        val json = JSONObject("""{"maxAttempts":3,"backoff":"fixed","baseDelayMs":100,"retryOn":[500,502]}""")

        val params = HttpRetryParams.deserializer.deserialize(json)

        assertEquals(3, params.maxAttempts)
        assertEquals(RetryBackoff.FIXED, params.backoff)
        assertEquals(100L, params.baseDelayMs)
        assertEquals(listOf(500, 502), params.retryOn)
    }

    @Test
    fun `deserializer should parse exponential backoff case-insensitively`() {
        val json = JSONObject("""{"backoff":"Exponential"}""")

        val params = HttpRetryParams.deserializer.deserialize(json)

        assertEquals(RetryBackoff.EXPONENTIAL, params.backoff)
    }

    @Test
    fun `deserializer should map an unrecognized backoff value to UNKNOWN`() {
        val json = JSONObject("""{"backoff":"linear"}""")

        val params = HttpRetryParams.deserializer.deserialize(json)

        assertEquals(RetryBackoff.UNKNOWN, params.backoff)
    }

    @Test
    fun `deserializer should default absent fields to null`() {
        val params = HttpRetryParams.deserializer.deserialize(JSONObject("{}"))

        assertNull(params.maxAttempts)
        assertNull(params.backoff)
        assertNull(params.baseDelayMs)
        assertNull(params.retryOn)
    }
}
