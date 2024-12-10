package io.primer.android.paypal

import io.mockk.every
import io.mockk.mockk
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PayPalPaymentMethodDescriptorFactoryTest {

    private lateinit var factory: PayPalPaymentMethodDescriptorFactory

    @BeforeEach
    fun setUp() {
        factory = PayPalPaymentMethodDescriptorFactory()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `create method with real params should create a PayPalDescriptor with the appropriate parameters`() {
        // Mock dependencies
        val localConfig: PrimerConfig = mockk()
        val paymentMethodRemoteConfig: PaymentMethodConfigDataResponse = mockk() {
            every { type } returns PaymentMethodType.PAYPAL.name
        }
        val paymentMethod: PayPal = mockk()
        val paymentMethodCheckers: PaymentMethodCheckerRegistry = mockk()

        // Call the method under test
        val result = factory.create(
            localConfig,
            paymentMethodRemoteConfig,
            paymentMethod,
            paymentMethodCheckers
        )

        val expectedDescriptor = PayPalDescriptor(
            localConfig = localConfig,
            config = paymentMethodRemoteConfig
        )

        // Verify that the PrimerTestPayPalDescriptor constructor was called with the expected arguments
        assertEquals(expectedDescriptor.config, result.config)
        assertEquals(expectedDescriptor.vaultCapability, result.vaultCapability)
        assertEquals(expectedDescriptor.headlessDefinition, result.headlessDefinition)
    }
}
