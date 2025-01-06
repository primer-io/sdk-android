package io.primer.android.sandboxProcessor.klarna

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertIs

@ExtendWith(MockKExtension::class)
class SandboxProcessorKlarnaDescriptorFactorySandboxProcessor {
    @InjectMockKs
    private lateinit var factory: SandboxProcessorKlarnaDescriptorFactory

    @Test
    fun `create() should return TestKlarnaPaymentMethodDescriptor`() {
        val descriptor =
            factory.create(
                localConfig = mockk(),
                paymentMethodRemoteConfig = mockk(),
                paymentMethod = mockk(),
                paymentMethodCheckers = mockk(),
            )
        assertIs<SandboxProcessorKlarnaPaymentMethodDescriptor>(descriptor)
    }
}
