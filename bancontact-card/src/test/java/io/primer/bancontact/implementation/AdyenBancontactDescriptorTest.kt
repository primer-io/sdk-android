package io.primer.bancontact.implementation

import io.mockk.mockk
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.bancontact.AdyenBancontactDescriptor
import io.primer.android.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.VaultCapability
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AdyenBancontactDescriptorTest {

    private lateinit var adyenBancontactDescriptor: AdyenBancontactDescriptor
    private lateinit var mockPrimerConfig: PrimerConfig
    private lateinit var mockConfig: PaymentMethodConfigDataResponse

    @BeforeEach
    fun setUp() {
        mockPrimerConfig = mockk(relaxed = true)
        mockConfig = mockk(relaxed = true)

        adyenBancontactDescriptor = AdyenBancontactDescriptor(mockPrimerConfig, mockConfig)
    }

    @Test
    fun `vaultCapability should be SINGLE_USE_ONLY`() {
        assertEquals(VaultCapability.SINGLE_USE_ONLY, adyenBancontactDescriptor.vaultCapability)
    }

    @Test
    fun `headlessDefinition should include RAW_DATA category`() {
        val expectedHeadlessDefinition = HeadlessDefinition(
            listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            HeadlessDefinition.RawDataDefinition(PrimerBancontactCardData::class)
        )
        assertEquals(
            expectedHeadlessDefinition,
            adyenBancontactDescriptor.headlessDefinition
        )
    }
}
