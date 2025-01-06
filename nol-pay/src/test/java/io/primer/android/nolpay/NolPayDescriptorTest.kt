package io.primer.android.nolpay

import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.VaultCapability
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NolPayDescriptorTest {
    private val localConfig: PrimerConfig = mockk()
    private val config: PaymentMethodConfigDataResponse = mockk()

    private val nolPayDescriptor = NolPayDescriptor(localConfig, config)

    @Test
    fun `should return SINGLE_USE_ONLY for vaultCapability`() {
        // When
        val result: VaultCapability = nolPayDescriptor.vaultCapability

        // Then
        assertEquals(VaultCapability.SINGLE_USE_ONLY, result)
    }

    @Test
    fun `should return headlessDefinition with NOL_PAY category`() {
        // When
        val result: HeadlessDefinition = nolPayDescriptor.headlessDefinition

        // Then
        assertEquals(1, result.paymentMethodManagerCategories.size)
        assertEquals(PrimerPaymentMethodManagerCategory.NOL_PAY, result.paymentMethodManagerCategories.first())
    }
}
