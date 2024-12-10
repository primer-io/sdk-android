package io.primer.android.sandboxProcessor.paypal

import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SandboxPayPalPaymentMethodDescriptorSandboxProcessorProcessor {
    @Test
    fun `should return correct HeadlessDefinition for Paypal`() {
        // Arrange
        val mockLocalConfig = mockk<PrimerConfig>()
        val mockConfig = mockk<PaymentMethodConfigDataResponse>()
        val payPalDescriptor = SandboxProcessorPayPalPaymentMethodDescriptor(mockLocalConfig, mockConfig)

        // Act
        val result = payPalDescriptor.headlessDefinition

        // Assert
        assertEquals(listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI), result?.paymentMethodManagerCategories)
    }
}
