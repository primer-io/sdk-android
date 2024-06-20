package io.primer.android.payment

import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodImplementationType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.mocks.MockPaymentMethodMapping
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class PaymentMethodListFactoryTest {

    private val configList: List<PaymentMethodConfigDataResponse> = listOf(
        PaymentMethodConfigDataResponse(
            null,
            PaymentMethodType.PAYMENT_CARD.name,
            PaymentMethodImplementationType.NATIVE_SDK,
            PaymentMethodType.UNKNOWN.name,
            null,
            null
        )
    )

    @Test
    fun `test maps correctly`() {
        val mapping = MockPaymentMethodMapping()
        val logReporter = mockk<LogReporter>(relaxed = true)
        val factory = PaymentMethodListFactory(mapping, logReporter)
        val paymentMethods = factory.buildWith(configList)
        assertTrue(mapping.getPaymentMethodForCalled)
        assertEquals(paymentMethods.size, 1)
    }

    @Test
    fun `test fails correctly`() {
        val mapping = MockPaymentMethodMapping(true)
        val logReporter = mockk<LogReporter>(relaxed = true)
        val factory = PaymentMethodListFactory(mapping, logReporter)
        val paymentMethods = factory.buildWith(configList)
        assertTrue(mapping.getPaymentMethodForCalled)
        assertEquals(paymentMethods.size, 0)
    }
}
