package io.primer.android.otp

import io.mockk.mockk
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.HeadlessDefinition
import io.primer.android.paymentmethods.SDKCapability
import io.primer.android.paymentmethods.VaultCapability
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OtpDescriptorTest {
    private lateinit var otpDescriptor: OtpDescriptor
    private lateinit var mockPrimerConfig: PrimerConfig
    private lateinit var mockConfig: PaymentMethodConfigDataResponse

    @BeforeEach
    fun setUp() {
        mockPrimerConfig = mockk(relaxed = true)
        mockConfig = mockk(relaxed = true)
    }

    private fun createOtpDescriptor(paymentMethodType: PaymentMethodType) =
        OtpDescriptor(
            localConfig = mockPrimerConfig,
            config = mockConfig,
            paymentMethodType = paymentMethodType.name,
        )

    @Test
    fun `vaultCapability should be SINGLE_USE_ONLY when payment method type is ADYEN_BLIK`() {
        otpDescriptor = createOtpDescriptor(PaymentMethodType.ADYEN_BLIK)
        assertEquals(VaultCapability.SINGLE_USE_ONLY, otpDescriptor.vaultCapability)
    }

    @Test
    fun `headlessDefinition should include RAW_DATA category when payment method type is ADYEN_BLIK`() {
        otpDescriptor = createOtpDescriptor(PaymentMethodType.ADYEN_BLIK)

        val expectedHeadlessDefinition =
            HeadlessDefinition(
                listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            )
        assertEquals(
            expectedHeadlessDefinition.paymentMethodManagerCategories,
            otpDescriptor.headlessDefinition.paymentMethodManagerCategories,
        )
    }

    @Test
    fun `sdkCapabilities should be HEADLESS and DROP-IN when payment method type is ADYEN_BLIK`() {
        otpDescriptor = createOtpDescriptor(PaymentMethodType.ADYEN_BLIK)

        assertEquals(listOf(SDKCapability.HEADLESS, SDKCapability.DROP_IN), otpDescriptor.sdkCapabilities)
    }
}
