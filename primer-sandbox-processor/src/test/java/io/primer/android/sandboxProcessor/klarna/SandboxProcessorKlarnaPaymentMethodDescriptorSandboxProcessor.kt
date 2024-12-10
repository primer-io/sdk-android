package io.primer.android.sandboxProcessor.klarna

import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SandboxProcessorKlarnaPaymentMethodDescriptorSandboxProcessor {
    @Test
    fun `should return correct HeadlessDefinition for Klarna`() {
        // Arrange
        val mockLocalConfig = mockk<PrimerConfig>()
        val mockConfig = mockk<PaymentMethodConfigDataResponse>()
        val klarnaDescriptor = SandboxProcessorKlarnaPaymentMethodDescriptor(mockLocalConfig, mockConfig)

        // Act
        val result = klarnaDescriptor.headlessDefinition

        // Assert
        assertEquals(listOf(PrimerPaymentMethodManagerCategory.KLARNA), result.paymentMethodManagerCategories)
    }
}
