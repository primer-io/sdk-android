package io.primer.checkout.orchestrator.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ProcessingResultTest {

    @Test
    fun `deserializer should parse delayMs when present`() {
        val json = actionJson().put("delayMs", 750L)

        val action = ResolvedAction.deserializer.deserialize(json)

        assertEquals(750L, action.delayMs)
    }

    @Test
    fun `deserializer should return null delayMs when absent`() {
        val action = ResolvedAction.deserializer.deserialize(actionJson())

        assertNull(action.delayMs)
    }

    @Test
    fun `deserializer should return null delayMs when JSON null`() {
        val json = actionJson().put("delayMs", JSONObject.NULL)

        val action = ResolvedAction.deserializer.deserialize(json)

        assertNull(action.delayMs)
    }

    @Test
    fun `deserializer should parse action fields`() {
        val action = ResolvedAction.deserializer.deserialize(actionJson())

        assertEquals("action-1", action.id)
        assertEquals("TOKENIZE", action.type)
        assertEquals("""{"token":"abc"}""", action.params)
    }

    private fun actionJson(): JSONObject = JSONObject().apply {
        put("id", "action-1")
        put("type", "TOKENIZE")
        put("params", """{"token":"abc"}""")
    }
}
