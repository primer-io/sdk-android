package io.primer.android.banks

import io.mockk.every
import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BankIssuerPaymentMethodDescriptorTest {

    @Test
    fun `test properties and methods`() {
        // Mock dependencies
        val options = mockk<BankIssuerPaymentMethod>()
        val localConfig = mockk<PrimerConfig>()
        val config = mockk<PaymentMethodConfigDataResponse>()

        // Mock internal behavior if necessary
        every { options.paymentMethodType } returns "BANK"
//        every { config.brand } returns mockk()
//        every { config.brand.logoResId } returns 123

        // Create instance
        val descriptor = BankIssuerPaymentMethodDescriptor(options, localConfig, config)

        // Verify headlessDefinition property
        val headlessDefinition = descriptor.headlessDefinition
        assertEquals(
            listOf(PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT),
            headlessDefinition.paymentMethodManagerCategories
        )
    }
}
