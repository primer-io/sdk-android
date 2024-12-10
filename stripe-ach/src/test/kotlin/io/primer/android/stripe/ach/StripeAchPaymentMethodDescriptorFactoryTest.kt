package io.primer.android.stripe.ach

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertIs

@ExtendWith(MockKExtension::class)
class StripeAchPaymentMethodDescriptorFactoryTest {
    @InjectMockKs
    private lateinit var factory: StripeAchPaymentMethodDescriptorFactory

    @Test
    fun `create() should return StripeAchDescriptor`() {
        val descriptor = factory.create(
            localConfig = mockk(),
            paymentMethodRemoteConfig = mockk(),
            paymentMethod = mockk(),
            paymentMethodCheckers = mockk()
        )
        assertIs<StripeAchDescriptor>(descriptor)
    }
}
