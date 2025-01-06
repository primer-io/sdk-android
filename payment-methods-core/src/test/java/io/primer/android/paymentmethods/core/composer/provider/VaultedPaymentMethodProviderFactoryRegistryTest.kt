package io.primer.android.paymentmethods.core.composer.provider

import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VaultedPaymentMethodProviderFactoryRegistryTest {
    private lateinit var registry: VaultedPaymentMethodProviderFactoryRegistry

    @BeforeEach
    fun setUp() {
        registry = VaultedPaymentMethodProviderFactoryRegistry()
    }

    class Factory : PaymentMethodComposerProvider.Factory {
        override fun create(
            paymentMethodType: String,
            sessionIntent: PrimerSessionIntent,
        ): PaymentMethodComposer = composer

        companion object {
            val composer = mockk<PaymentMethodComposer>()
        }
    }

    @Test
    fun `create() returns PaymentMethodComposer when factory is registered`() {
        val paymentMethodType = "KLARNA"
        val sessionIntent = mockk<PrimerSessionIntent>()

        registry.register(paymentMethodType, Factory::class.java)

        val result = registry.create(paymentMethodType, sessionIntent)

        assertEquals(Factory.composer, result)
    }

    @Test
    fun `create() returns null when factory is not registered`() {
        val paymentMethodType = "KLARNA"
        val sessionIntent = mockk<PrimerSessionIntent>()

        val result = registry.create(paymentMethodType, sessionIntent)

        assertEquals(null, result)
    }
}
