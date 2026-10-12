package io.primer.executionengine.data.registry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class DefaultUiComponentRegistryTest {
    private val registry = DefaultUiComponentRegistry()

    @Test
    fun `declares nothing until a host declares`() {
        assertTrue(registry.declaredVersions.isEmpty())
    }

    @Test
    fun `declares each type at its version, the last declaration winning`() {
        registry.declare("SelectionGroup", "1.0.0")
        registry.declare("klarna.widget", "1.0.0")
        registry.declare("klarna.widget", "2.0.0")

        assertEquals(mapOf("SelectionGroup" to "1.0.0", "klarna.widget" to "2.0.0"), registry.declaredVersions)
    }

    @Test
    fun `a read is a snapshot`() {
        registry.declare("Button", "1.0.0")
        val read = registry.declaredVersions

        registry.declare("Text", "1.0.0")

        assertEquals(setOf("Button"), read.keys)
    }
}
