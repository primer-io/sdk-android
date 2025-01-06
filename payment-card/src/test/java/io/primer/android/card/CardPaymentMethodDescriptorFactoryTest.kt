package io.primer.android.card

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CardPaymentMethodDescriptorFactoryTest {
    private lateinit var factory: CardPaymentMethodDescriptorFactory

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        factory = CardPaymentMethodDescriptorFactory()
    }

    @Test
    fun `create method with real params should create a CreditCard with the appropriate parameters`() {
        // Mock dependencies
        val localConfig: PrimerConfig = mockk()
        val paymentMethodRemoteConfig: PaymentMethodConfigDataResponse =
            mockk {
                every { type } returns PaymentMethodType.PAYPAL.name
            }
        val paymentMethod: Card = mockk()
        val paymentMethodCheckers: PaymentMethodCheckerRegistry = mockk()

        // Call the method under test
        val result =
            factory.create(
                localConfig,
                paymentMethodRemoteConfig,
                paymentMethod,
                paymentMethodCheckers,
            )

        val expectedDescriptor =
            CreditCard(
                localConfig = localConfig,
                config = paymentMethodRemoteConfig,
            )

        Assertions.assertEquals(expectedDescriptor.config, result.config)
        Assertions.assertEquals(expectedDescriptor.vaultCapability, result.vaultCapability)
    }
}
