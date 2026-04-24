package io.primer.executionengine.data.models.http

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HttpExecutionResultTest {

    @Test
    fun `serialize should include all fields`() {
        val result = HttpExecutionResult(
            statusCode = 200,
            response = """{"ok":true}""",
            headers = mapOf("Content-Type" to listOf("application/json")),
        )

        val json = HttpExecutionResult.serializer.serialize(result)

        assertEquals(200, json.getInt("statusCode"))
        assertEquals("""{"ok":true}""", json.getString("response"))
        assertTrue(json.has("headers"))
    }

    @Test
    fun `serialize should handle null statusCode and response`() {
        val result = HttpExecutionResult()

        val json = HttpExecutionResult.serializer.serialize(result)

        assertTrue(json.isNull("statusCode"))
        assertTrue(json.isNull("response"))
    }

    @Test
    fun `serialize should handle empty headers`() {
        val result = HttpExecutionResult(
            statusCode = 204,
            headers = emptyMap(),
        )

        val json = HttpExecutionResult.serializer.serialize(result)

        assertEquals(204, json.getInt("statusCode"))
        assertTrue(json.has("headers"))
    }

    @Test
    fun `serialize should handle multiple header values`() {
        val result = HttpExecutionResult(
            statusCode = 200,
            response = "body",
            headers = mapOf(
                "X-Request-Id" to listOf("req-123"),
                "Set-Cookie" to listOf("a=1", "b=2"),
            ),
        )

        val json = HttpExecutionResult.serializer.serialize(result)

        assertEquals(200, json.getInt("statusCode"))
        assertEquals("body", json.getString("response"))
    }
}
