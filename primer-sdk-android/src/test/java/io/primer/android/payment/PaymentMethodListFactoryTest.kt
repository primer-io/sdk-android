package io.primer.android.payment

import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfig
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.logging.DefaultLogger
import io.primer.android.mocks.MockPaymentMethodMapping
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class PaymentMethodListFactoryTest {

    private val configList: List<PaymentMethodRemoteConfig> = listOf(
        PaymentMethodRemoteConfig(type = PaymentMethodType.UNKNOWN),
    )

    @Test
    fun `test maps correctly`() {
        val mapping = MockPaymentMethodMapping()
        val factory = PaymentMethodListFactory(mapping)
        val paymentMethods = factory.buildWith(configList)
        Assert.assertTrue(mapping.getPaymentMethodForCalled)
        Assert.assertEquals(paymentMethods.size, 1)
    }

    @Test
    fun `test fails correctly`() {
        val mapping = MockPaymentMethodMapping(true)
        val logger = mockk<DefaultLogger>(relaxed = true)
        val factory = PaymentMethodListFactory(mapping, logger)
        val paymentMethods = factory.buildWith(configList)
        Assert.assertTrue(mapping.getPaymentMethodForCalled)
        Assert.assertEquals(paymentMethods.size, 0)
    }
}
