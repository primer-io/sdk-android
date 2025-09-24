package io.primer.android.components.analytics.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AnalyticsResponseTest {

    @Test
    fun `deserializer should parse valid JSON with all fields`() {
        val json = JSONObject().apply {
            put("id", "response-123")
            put("result", "processed")
        }

        val response = AnalyticsResponse.deserializer.deserialize(json)

        assertEquals("response-123", response.id)
        assertEquals("processed", response.result)
    }

    @Test
    fun `deserializer should handle null id`() {
        val json = JSONObject().apply {
            put("id", JSONObject.NULL)
            put("result", "processed")
        }

        val response = AnalyticsResponse.deserializer.deserialize(json)

        assertNull(response.id)
        assertEquals("processed", response.result)
    }

    @Test
    fun `deserializer should handle null result`() {
        val json = JSONObject().apply {
            put("id", "response-456")
            put("result", JSONObject.NULL)
        }

        val response = AnalyticsResponse.deserializer.deserialize(json)

        assertEquals("response-456", response.id)
        assertNull(response.result)
    }

    @Test
    fun `deserializer should handle empty strings as null`() {
        val json = JSONObject().apply {
            put("id", "")
            put("result", "")
        }

        val response = AnalyticsResponse.deserializer.deserialize(json)

        assertNull(response.id)
        assertNull(response.result)
    }

    @Test
    fun `deserializer should handle missing fields`() {
        val json = JSONObject()

        val response = AnalyticsResponse.deserializer.deserialize(json)

        assertNull(response.id)
        assertNull(response.result)
    }

    @Test
    fun `deserializer should handle both fields null`() {
        val json = JSONObject().apply {
            put("id", JSONObject.NULL)
            put("result", JSONObject.NULL)
        }

        val response = AnalyticsResponse.deserializer.deserialize(json)

        assertNull(response.id)
        assertNull(response.result)
    }

    @Test
    fun `data class should have correct properties`() {
        val response = AnalyticsResponse(
            id = "test-id",
            result = "success",
        )

        assertEquals("test-id", response.id)
        assertEquals("success", response.result)
    }

    @Test
    fun `data class should support null values`() {
        val response = AnalyticsResponse(
            id = null,
            result = null,
        )

        assertNull(response.id)
        assertNull(response.result)
    }
}
