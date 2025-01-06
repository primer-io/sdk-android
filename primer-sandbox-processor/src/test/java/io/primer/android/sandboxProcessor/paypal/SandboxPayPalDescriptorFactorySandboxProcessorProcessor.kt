package io.primer.android.sandboxProcessor.paypal

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertIs

@ExtendWith(MockKExtension::class)
class SandboxPayPalDescriptorFactorySandboxProcessorProcessor {
    @InjectMockKs
    private lateinit var factory: SandboxProcessorPayPalDescriptorFactory

    @Test
    fun `create() should return TestPayPalPaymentMethodDescriptor`() {
        val descriptor =
            factory.create(
                localConfig = mockk(),
                paymentMethodRemoteConfig = mockk(),
                paymentMethod = mockk(),
                paymentMethodCheckers = mockk(),
            )
        assertIs<SandboxProcessorPayPalPaymentMethodDescriptor>(descriptor)
    }
}
