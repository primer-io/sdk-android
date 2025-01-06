package io.primer.android.phoneNumber

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

internal class PhoneNumberDescriptorTest {
    private lateinit var phoneNumberDescriptor: PhoneNumberDescriptor
    private lateinit var mockPrimerConfig: PrimerConfig
    private lateinit var mockConfig: PaymentMethodConfigDataResponse

    @BeforeEach
    fun setUp() {
        mockPrimerConfig = mockk(relaxed = true)
        mockConfig = mockk(relaxed = true)
    }

    private fun createPhoneNumberDescriptor(paymentMethodType: PaymentMethodType) =
        PhoneNumberDescriptor(
            localConfig = mockPrimerConfig,
            config = mockConfig,
            paymentMethodType = paymentMethodType.name,
        )

    @Test
    fun `vaultCapability should be SINGLE_USE_ONLY when payment method type is ADYEN_MBWAY`() {
        phoneNumberDescriptor = createPhoneNumberDescriptor(PaymentMethodType.ADYEN_MBWAY)
        assertEquals(VaultCapability.SINGLE_USE_ONLY, phoneNumberDescriptor.vaultCapability)
    }

    @Test
    fun `vaultCapability should be SINGLE_USE_ONLY when payment method type is XENDIT_OVO`() {
        phoneNumberDescriptor = createPhoneNumberDescriptor(PaymentMethodType.XENDIT_OVO)
        assertEquals(VaultCapability.SINGLE_USE_ONLY, phoneNumberDescriptor.vaultCapability)
    }

    @Test
    fun `headlessDefinition should include RAW_DATA category when payment method type is ADYEN_MBWAY`() {
        phoneNumberDescriptor = createPhoneNumberDescriptor(PaymentMethodType.ADYEN_MBWAY)

        val expectedHeadlessDefinition =
            HeadlessDefinition(
                listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            )
        assertEquals(
            expectedHeadlessDefinition.paymentMethodManagerCategories,
            phoneNumberDescriptor.headlessDefinition.paymentMethodManagerCategories,
        )
    }

    @Test
    fun `headlessDefinition should include RAW_DATA category when payment method type is XENDIT_OVO`() {
        phoneNumberDescriptor = createPhoneNumberDescriptor(PaymentMethodType.XENDIT_OVO)

        val expectedHeadlessDefinition =
            HeadlessDefinition(
                listOf(PrimerPaymentMethodManagerCategory.RAW_DATA),
            )
        assertEquals(
            expectedHeadlessDefinition.paymentMethodManagerCategories,
            phoneNumberDescriptor.headlessDefinition.paymentMethodManagerCategories,
        )
    }

    @Test
    fun `sdkCapabilities should be HEADLESS and DROP-IN when payment method type is ADYEN_MBWAY`() {
        phoneNumberDescriptor = createPhoneNumberDescriptor(PaymentMethodType.ADYEN_MBWAY)

        assertEquals(listOf(SDKCapability.HEADLESS, SDKCapability.DROP_IN), phoneNumberDescriptor.sdkCapabilities)
    }

    @Test
    fun `sdkCapabilities should be HEADLESS when payment method type is XENDIT_OVO`() {
        phoneNumberDescriptor = createPhoneNumberDescriptor(PaymentMethodType.XENDIT_OVO)

        assertEquals(listOf(SDKCapability.HEADLESS), phoneNumberDescriptor.sdkCapabilities)
    }
}
