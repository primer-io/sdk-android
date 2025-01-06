package io.primer.android.qrcode

import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.VaultCapability
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class QrCodeDescriptorTest {
    private lateinit var qrCodeDescriptor: QrCodeDescriptor
    private lateinit var mockPrimerConfig: PrimerConfig
    private lateinit var mockConfig: PaymentMethodConfigDataResponse

    @BeforeEach
    fun setUp() {
        mockPrimerConfig = mockk(relaxed = true)
        mockConfig = mockk(relaxed = true)

        qrCodeDescriptor = QrCodeDescriptor(mockPrimerConfig, mockConfig)
    }

    @Test
    fun `vaultCapability should be SINGLE_USE_ONLY`() {
        assertEquals(VaultCapability.SINGLE_USE_ONLY, qrCodeDescriptor.vaultCapability)
    }

    @Test
    fun `headlessDefinition should include NATIVE_UI category`() {
        val expectedHeadlessDefinition =
            HeadlessDefinition(
                listOf(PrimerPaymentMethodManagerCategory.NATIVE_UI),
            )
        assertEquals(expectedHeadlessDefinition, qrCodeDescriptor.headlessDefinition)
    }
}
