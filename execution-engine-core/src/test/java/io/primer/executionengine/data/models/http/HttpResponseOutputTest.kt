package io.primer.executionengine.data.models.http

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HttpResponseOutputTest {

    @Test
    fun `serializer should produce status success headers and body fields`() {
        val output = HttpResponseOutput(
            status = 200,
            success = true,
            headers = mapOf("content-type" to "application/json"),
            body = mapOf("a" to 1),
        )

        val json = HttpResponseOutput.serializer.serialize(output)

        assertEquals(200, json.getInt("status"))
        assertEquals(true, json.getBoolean("success"))
        assertEquals("application/json", json.getJSONObject("headers").getString("content-type"))
        assertEquals(1, json.getJSONObject("body").getInt("a"))
    }

    @Test
    fun `serializer should write an explicit JSON null for a null body`() {
        val output = HttpResponseOutput(
            status = 204,
            success = true,
            headers = emptyMap(),
            body = null,
        )

        val json = HttpResponseOutput.serializer.serialize(output)

        assertTrue(json.has("body"))
        assertTrue(json.isNull("body"))
    }

    @Test
    fun `toDataMap should produce exactly the status success headers and body keys`() {
        val headers = mapOf("x-request-id" to "req-1")
        val output = HttpResponseOutput(
            status = 404,
            success = false,
            headers = headers,
            body = "not found",
        )

        val data = output.toDataMap()

        assertEquals(
            mapOf(
                "status" to 404,
                "success" to false,
                "headers" to headers,
                "body" to "not found",
            ),
            data,
        )
        assertEquals(setOf("status", "success", "headers", "body"), data.keys)
    }
}
