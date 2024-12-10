package io.primer.android.vouchers.retailOutlets

import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.VaultCapability
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RetailOutletsDescriptorTest {

    private lateinit var retailOutletsDescriptor: RetailOutletsDescriptor
    private lateinit var mockPrimerConfig: PrimerConfig
    private lateinit var mockConfig: PaymentMethodConfigDataResponse

    @BeforeEach
    fun setUp() {
        mockPrimerConfig = mockk(relaxed = true)
        mockConfig = mockk(relaxed = true)

        retailOutletsDescriptor = RetailOutletsDescriptor(mockPrimerConfig, mockConfig)
    }

    @Test
    fun `vaultCapability should be SINGLE_USE_ONLY`() {
        assertEquals(VaultCapability.SINGLE_USE_ONLY, retailOutletsDescriptor.vaultCapability)
    }

    @Test
    fun `headlessDefinition should include RAW_DATA category`() {
        assertTrue(
            retailOutletsDescriptor.headlessDefinition.paymentMethodManagerCategories.contains(
                PrimerPaymentMethodManagerCategory.RAW_DATA
            )
        )
    }
}
