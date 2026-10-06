package io.primer.android.paymentmethods

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PrimerPaymentMethodCheckerRegistryTest {
    private lateinit var registry: PrimerPaymentMethodCheckerRegistry

    @BeforeEach
    fun setUp() {
        registry = PrimerPaymentMethodCheckerRegistry()
    }

    @Test
    fun `get() returns the registered checker`() {
        val checker = mockk<PaymentMethodChecker>()

        registry.register(PAYMENT_METHOD_TYPE, checker)

        assertEquals(checker, registry[PAYMENT_METHOD_TYPE])
    }

    @Test
    fun `get() returns null once the checker is unregistered`() {
        registry.register(PAYMENT_METHOD_TYPE, mockk())

        registry.unregister(PAYMENT_METHOD_TYPE)

        assertNull(registry[PAYMENT_METHOD_TYPE])
    }

    @Test
    fun `checkers registered in one session are not visible to the next`() {
        registry.register(PAYMENT_METHOD_TYPE, mockk())

        val nextSessionRegistry = PrimerPaymentMethodCheckerRegistry()

        assertNull(nextSessionRegistry[PAYMENT_METHOD_TYPE])
        assertEquals(emptyMap<String, PaymentMethodChecker>(), nextSessionRegistry.checkers)
    }

    private companion object {
        const val PAYMENT_METHOD_TYPE = "PAYMENT_METHOD"
    }
}
