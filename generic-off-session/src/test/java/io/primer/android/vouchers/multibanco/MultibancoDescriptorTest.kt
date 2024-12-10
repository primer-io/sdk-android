package io.primer.android.vouchers.multibanco

import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.VaultCapability
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MultibancoDescriptorTest {

    private lateinit var multibancoDescriptor: MultibancoDescriptor
    private lateinit var mockPrimerConfig: PrimerConfig
    private lateinit var mockConfig: PaymentMethodConfigDataResponse

    @BeforeEach
    fun setUp() {
        mockPrimerConfig = mockk(relaxed = true)
        mockConfig = mockk(relaxed = true)

        multibancoDescriptor = MultibancoDescriptor(mockPrimerConfig, mockConfig)
    }

    @Test
    fun `vaultCapability should be SINGLE_USE_ONLY`() {
        assertEquals(VaultCapability.SINGLE_USE_ONLY, multibancoDescriptor.vaultCapability)
    }

    @Test
    fun `headlessDefinition should include NATIVE_UI category`() {
        val expectedHeadlessDefinition = HeadlessDefinition(
            listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI)
        )
        assertEquals(expectedHeadlessDefinition, multibancoDescriptor.headlessDefinition)
    }
}
