package io.primer.android.googlepay

import io.mockk.mockk
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GooglePayPaymentMethodDescriptorFactoryTest {

    private lateinit var factory: GooglePayPaymentMethodDescriptorFactory

    @BeforeEach
    fun setUp() {
        factory = GooglePayPaymentMethodDescriptorFactory()
    }

    @Test
    fun `create method should create a GooglePayDescriptor with the appropriate parameters`() {
        // Mock dependencies
        val localConfig: PrimerConfig = mockk()
        val paymentMethodRemoteConfig: PaymentMethodConfigDataResponse = mockk()
        val paymentMethod: GooglePay = mockk()
        val paymentMethodCheckers: PaymentMethodCheckerRegistry = mockk()

        // Call the method under test
        val result = factory.create(
            localConfig,
            paymentMethodRemoteConfig,
            paymentMethod,
            paymentMethodCheckers
        )

        val expectedDescriptor = GooglePayDescriptor(
            localConfig = localConfig,
            options = paymentMethod,
            config = paymentMethodRemoteConfig
        )

        // Verify that the GooglePayDescriptor constructor was called with the expected arguments
        assertEquals(expectedDescriptor.options, result.options)
        assertEquals(expectedDescriptor.config, result.config)
        assertEquals(expectedDescriptor.vaultCapability, result.vaultCapability)
        assertEquals(expectedDescriptor.headlessDefinition, result.headlessDefinition)
    }
}
