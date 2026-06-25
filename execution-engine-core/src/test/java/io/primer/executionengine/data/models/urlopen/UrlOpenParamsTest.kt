package io.primer.executionengine.data.models.urlopen

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class UrlOpenParamsTest {

    @Test
    fun `deserialize should parse url only`() {
        val json = JSONObject().apply {
            put("url", "https://example.com")
        }

        val result = UrlOpenParams.deserializer.deserialize(json)

        assertEquals("https://example.com", result.url)
        assertNull(result.redirectUrls)
    }

    @Test
    fun `deserialize should parse url with redirectUrls`() {
        val json = JSONObject().apply {
            put("url", "https://example.com")
            put("redirectUrls", JSONArray(listOf("https://return1.com", "https://return2.com")))
        }

        val result = UrlOpenParams.deserializer.deserialize(json)

        assertEquals("https://example.com", result.url)
        assertEquals(listOf("https://return1.com", "https://return2.com"), result.redirectUrls)
    }

    @Test
    fun `deserialize should parse url with webview config`() {
        val json = JSONObject().apply {
            put("url", "https://pay.example.com")
            put("webview", JSONObject().put("title", "Payment"))
        }

        val result = UrlOpenParams.deserializer.deserialize(json)

        assertEquals("https://pay.example.com", result.url)
        assertNull(result.redirectUrls)
    }

    @Test
    fun `deserialize should parse all fields`() {
        val json = JSONObject().apply {
            put("url", "https://pay.example.com")
            put("redirectUrls", JSONArray(listOf("https://return.com")))
            put("webview", JSONObject().put("title", "Pay Now"))
        }

        val result = UrlOpenParams.deserializer.deserialize(json)

        assertEquals("https://pay.example.com", result.url)
        assertEquals(listOf("https://return.com"), result.redirectUrls)
    }

    @Test
    fun `deserialize should throw when url is missing`() {
        val json = JSONObject().apply {
            put("redirectUrls", JSONArray())
        }

        assertThrows(Exception::class.java) {
            UrlOpenParams.deserializer.deserialize(json)
        }
    }

    @Test
    fun `deserialize should handle empty redirectUrls array`() {
        val json = JSONObject().apply {
            put("url", "https://example.com")
            put("redirectUrls", JSONArray())
        }

        val result = UrlOpenParams.deserializer.deserialize(json)

        assertEquals(emptyList<String>(), result.redirectUrls)
    }
}
