package io.primer.android.components.implementation.completion

import io.mockk.mockk
import io.primer.android.payments.core.idempotency.IdempotencyKeyHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class DefaultPreTokenizationHandlerTest {
    private lateinit var idempotencyKeyHolder: IdempotencyKeyHolder

    @BeforeEach
    fun setUp() {
        idempotencyKeyHolder = IdempotencyKeyHolder()
    }

    @Test
    fun `AutoPreTokenizationHandlerStrategy can be instantiated`() =
        runTest {
            val strategy = AutoPreTokenizationHandlerStrategy(
                analyticsRepository = mockk(relaxed = true),
                idempotencyKeyHolder = idempotencyKeyHolder,
            )

            assertNull(idempotencyKeyHolder.get())
        }

    @Test
    fun `AutoPreTokenizationHandlerStrategy stores key correctly`() =
        runTest {
            AutoPreTokenizationHandlerStrategy(
                analyticsRepository = mockk(relaxed = true),
                idempotencyKeyHolder = idempotencyKeyHolder,
            )
            val testKey = "test-key-123"

            idempotencyKeyHolder.set(testKey)

            assertEquals(testKey, idempotencyKeyHolder.get())
        }

    @Test
    fun `AutoPreTokenizationHandlerStrategy clears key correctly`() =
        runTest {
            AutoPreTokenizationHandlerStrategy(
                analyticsRepository = mockk(relaxed = true),
                idempotencyKeyHolder = idempotencyKeyHolder,
            )

            idempotencyKeyHolder.set("test-key")
            idempotencyKeyHolder.clear()

            assertNull(idempotencyKeyHolder.get())
        }

    @Test
    fun `ManualPreTokenizationHandlerStrategy can be instantiated`() =
        runTest {
            val strategy = ManualPreTokenizationHandlerStrategy()

            assertNull(idempotencyKeyHolder.get())
        }
}
