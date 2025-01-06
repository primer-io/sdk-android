package io.primer.android.klarna

import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KlarnaPaymentMethodDescriptorFactoryTest {
    private lateinit var factory: KlarnaPaymentMethodDescriptorFactory
    private lateinit var localConfig: PrimerConfig
    private lateinit var paymentMethodRemoteConfig: PaymentMethodConfigDataResponse
    private lateinit var paymentMethod: Klarna
    private lateinit var paymentMethodCheckers: PaymentMethodCheckerRegistry

    @BeforeEach
    fun setUp() {
        factory = KlarnaPaymentMethodDescriptorFactory()
        localConfig = mockk(relaxed = true)
        paymentMethodRemoteConfig = mockk(relaxed = true)
        paymentMethod = mockk(relaxed = true)
        paymentMethodCheckers = mockk(relaxed = true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `create should return KlarnaDescriptor when type is not PRIMER_TEST_KLARNA`() {
        every { paymentMethodRemoteConfig.type } returns "some_other_type"

        val descriptor = factory.create(localConfig, paymentMethodRemoteConfig, paymentMethod, paymentMethodCheckers)

        assertTrue(descriptor is KlarnaDescriptor)
    }
}
