package io.primer.android.payments.core.idempotency

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IdempotencyKeyHolderTest {
    private lateinit var holder: IdempotencyKeyHolder

    @BeforeEach
    fun setUp() {
        holder = IdempotencyKeyHolder()
    }

    @Test
    fun `get should return null when no key is set`() {
        assertNull(holder.get())
    }

    @Test
    fun `set and get should store and retrieve key`() {
        val testKey = "test-idempotency-key"
        holder.set(testKey)
        assertEquals(testKey, holder.get())
    }

    @Test
    fun `set with null should clear the key`() {
        holder.set("test-key")
        holder.set(null)
        assertNull(holder.get())
    }

    @Test
    fun `clear should remove the stored key`() {
        holder.set("test-key")
        holder.clear()
        assertNull(holder.get())
    }

    @Test
    fun `set should overwrite previous key`() {
        holder.set("first-key")
        holder.set("second-key")
        assertEquals("second-key", holder.get())
    }
}
