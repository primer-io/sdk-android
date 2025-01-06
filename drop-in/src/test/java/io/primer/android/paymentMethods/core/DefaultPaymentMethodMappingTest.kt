package io.primer.android.paymentMethods.core

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.primer.android.PrimerSessionIntent
import io.primer.android.assets.ui.model.Brand
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentMethods.bancontact.AdyenBancontactDropInDescriptor
import io.primer.android.paymentMethods.banks.descriptor.BankIssuerDropInDescriptor
import io.primer.android.paymentMethods.card.descriptors.CardDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.TestDropInPaymentMethodDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentMethods.klarna.descriptors.KlarnaDropInDescriptor
import io.primer.android.paymentMethods.multibanco.AdyenMultibancoDropInDescriptor
import io.primer.android.paymentMethods.nativeUi.descriptors.NativeUiDropInDescriptor
import io.primer.android.paymentMethods.otp.OtpDropInDescriptor
import io.primer.android.paymentMethods.paypal.TestPayPalDropInPaymentMethodDescriptor
import io.primer.android.paymentMethods.phoneNumber.descriptor.PhoneNumberDropInDescriptor
import io.primer.android.paymentMethods.sofort.TestSofortDropInPaymentMethodDescriptor
import io.primer.android.paymentMethods.stripe.ach.descriptors.StripeAchDropInDescriptor
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultPaymentMethodMappingTest {
    private lateinit var paymentMethodMapping: DefaultPaymentMethodMapping

    @MockK
    private lateinit var config: PrimerConfig

    @MockK
    private lateinit var brandRegistry: BrandRegistry

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { config.isStandalonePaymentMethod } returns true
        every { config.settings.uiOptions.isInitScreenEnabled } returns true
        every { config.settings.uiOptions.theme.isDarkMode } returns true
        every { config.paymentMethodIntent } returns PrimerSessionIntent.CHECKOUT
        paymentMethodMapping = DefaultPaymentMethodMapping(config, brandRegistry)
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return AdyenMultibancoDropInDescriptor for NATIVE_UI category and ADYEN_MULTIBANCO`() {
        val paymentMethodType = PaymentMethodType.ADYEN_MULTIBANCO.name
        every { brandRegistry.getBrand(any()) } returns mockk(relaxed = true)
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = null,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.NATIVE_UI,
            )

        assertIs<AdyenMultibancoDropInDescriptor>(result)
        assertEquals(
            paymentMethodType,
            result.paymentMethodType,
        )
        assertEquals(
            UiOptions(
                isStandalonePaymentMethod = true,
                isInitScreenEnabled = true,
                isDarkMode = true,
            ),
            result.uiOptions,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return TestSofortDropInPaymentMethodDescriptor for NATIVE_UI category and PRIMER_TEST_SOFORT`() {
        val paymentMethodType = PaymentMethodType.PRIMER_TEST_SOFORT.name
        every { brandRegistry.getBrand(any()) } returns mockk(relaxed = true)
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = null,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.NATIVE_UI,
            )

        assertIs<TestSofortDropInPaymentMethodDescriptor>(result)
        assertEquals(
            paymentMethodType,
            result.paymentMethodType,
        )
        assertEquals(
            UiOptions(
                isStandalonePaymentMethod = true,
                isInitScreenEnabled = true,
                isDarkMode = true,
            ),
            result.uiOptions,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return TestPayPalDropInPaymentMethodDescriptor for NATIVE_UI category and PRIMER_TEST_PAYPAL`() {
        val paymentMethodType = PaymentMethodType.PRIMER_TEST_PAYPAL.name
        every { brandRegistry.getBrand(any()) } returns mockk(relaxed = true)
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = null,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.NATIVE_UI,
            )

        assertIs<TestPayPalDropInPaymentMethodDescriptor>(result)
        assertEquals(
            paymentMethodType,
            result.paymentMethodType,
        )
        assertEquals(
            UiOptions(
                isStandalonePaymentMethod = true,
                isInitScreenEnabled = true,
                isDarkMode = true,
            ),
            result.uiOptions,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return NativeUiDropInDescriptor for NATIVE_UI category`() {
        val paymentMethodType = "SomePaymentMethod"
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = null,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.NATIVE_UI,
            )

        assertEquals(
            NativeUiDropInDescriptor(
                paymentMethodType = paymentMethodType,
                uiOptions =
                    UiOptions(
                        isStandalonePaymentMethod = true,
                        isInitScreenEnabled = true,
                        isDarkMode = true,
                    ),
                primerSessionIntent = PrimerSessionIntent.CHECKOUT,
                brandRegistry = brandRegistry,
            ),
            result,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return PhoneNumberDropInDescriptor for RAW_DATA category and ADYEN_MBWAY`() {
        val paymentMethodType = PaymentMethodType.ADYEN_MBWAY.name
        val paymentMethodName = "Adyen MBWAY"
        every { brandRegistry.getBrand(any()) } returns mockk(relaxed = true)
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = paymentMethodName,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.RAW_DATA,
            )

        assertEquals(
            PhoneNumberDropInDescriptor(
                paymentMethodType = paymentMethodType,
                uiOptions =
                    UiOptions(
                        isStandalonePaymentMethod = true,
                        isInitScreenEnabled = true,
                        isDarkMode = true,
                    ),
                brandRegistry = brandRegistry,
                paymentMethodName = paymentMethodName,
            ),
            result,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return AdyenBancontactDropInDescriptor for RAW_DATA category and ADYEN_BANCONTACT_CARD`() {
        val paymentMethodType = PaymentMethodType.ADYEN_BANCONTACT_CARD.name
        val paymentMethodName = "Adyen Bancontact"
        every { brandRegistry.getBrand(any()) } returns mockk(relaxed = true)
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = paymentMethodName,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.RAW_DATA,
            )

        assertIs<AdyenBancontactDropInDescriptor>(result)
        assertEquals(
            UiOptions(
                isStandalonePaymentMethod = true,
                isInitScreenEnabled = true,
                isDarkMode = true,
            ),
            result.uiOptions,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return OtpDropInDescriptor for RAW_DATA category and ADYEN_BLIK`() {
        val paymentMethodType = PaymentMethodType.ADYEN_BLIK.name
        val paymentMethodName = "Adyen Blik"
        every { brandRegistry.getBrand(any()) } returns mockk(relaxed = true)
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = paymentMethodName,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.RAW_DATA,
            )

        assertIs<OtpDropInDescriptor>(result)
        assertEquals(
            paymentMethodType,
            result.paymentMethodType,
        )
        assertEquals(
            UiOptions(
                isStandalonePaymentMethod = true,
                isInitScreenEnabled = true,
                isDarkMode = true,
            ),
            result.uiOptions,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return CardDropInDescriptor for RAW_DATA category and non-specific payment methods`() {
        val paymentMethodType = "OTHER_PAYMENT_METHOD"
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = null,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.RAW_DATA,
            )

        assertEquals(
            CardDropInDescriptor(
                paymentMethodType = paymentMethodType,
                uiOptions =
                    UiOptions(
                        isStandalonePaymentMethod = true,
                        isInitScreenEnabled = true,
                        isDarkMode = true,
                    ),
            ),
            result,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return StripeAchDropInDescriptor for STRIPE_ACH category`() {
        val paymentMethodType = PaymentMethodType.STRIPE_ACH.name
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = null,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.STRIPE_ACH,
            )

        assertEquals(
            StripeAchDropInDescriptor(
                paymentMethodType = paymentMethodType,
                uiOptions =
                    UiOptions(
                        isStandalonePaymentMethod = true,
                        isInitScreenEnabled = true,
                        isDarkMode = true,
                    ),
            ),
            result,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return BankIssuerDropInDescriptor for COMPONENT_WITH_REDIRECT category`() {
        val paymentMethodType = PaymentMethodType.ADYEN_IDEAL.name
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = null,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT,
            )

        assertEquals(
            BankIssuerDropInDescriptor(
                paymentMethodType = paymentMethodType,
                uiOptions =
                    UiOptions(
                        isStandalonePaymentMethod = true,
                        isInitScreenEnabled = true,
                        isDarkMode = true,
                    ),
            ),
            result,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return KlarnaDropInDescriptor for KLARNA category`() {
        val paymentMethodType = PaymentMethodType.KLARNA.name
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = null,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.KLARNA,
            )

        assertEquals(
            KlarnaDropInDescriptor(
                paymentMethodType = paymentMethodType,
                uiOptions =
                    UiOptions(
                        isStandalonePaymentMethod = true,
                        isInitScreenEnabled = true,
                        isDarkMode = true,
                    ),
            ),
            result,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should return TestKlarnaDropInPaymentMethodDescriptor for PRIMER_TEST_KLARNA category`() {
        val paymentMethodType = PaymentMethodType.PRIMER_TEST_KLARNA.name
        val brand = mockk<Brand>()
        every { brandRegistry.getBrand(any()) } returns brand
        val result =
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = paymentMethodType,
                paymentMethodName = null,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.KLARNA,
            ) as TestDropInPaymentMethodDescriptor
        assertEquals(
            paymentMethodType,
            result.paymentMethodType,
        )
        assertEquals(
            UiOptions(
                isStandalonePaymentMethod = true,
                isInitScreenEnabled = true,
                isDarkMode = true,
            ),
            result.uiOptions,
        )
        assertEquals(
            brand,
            result.brand,
        )
    }

    @Test
    fun `getPaymentMethodDescriptorFor() should throw an exception for unsupported category (NOL_PAY)`() {
        assertThrows<IllegalStateException> {
            paymentMethodMapping.getPaymentMethodDescriptorFor(
                paymentMethodType = "unsupported_method",
                paymentMethodName = null,
                paymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.NOL_PAY,
            )
        }
    }
}
