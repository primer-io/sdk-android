package io.primer.android.phoneNumber

import android.content.Context
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.phoneNumber.implementation.composer.presentation.provider.PhoneNumberComposerProviderFactory
import io.primer.android.phoneNumber.implementation.composer.ui.assets.MbWayBrand
import io.primer.android.phoneNumber.implementation.composer.ui.assets.XenditOvoBrand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PhoneNumberTest {
    private val paymentMethodType = "phoneNumber"
    private lateinit var phoneNumber: PhoneNumber
    private lateinit var mockContext: Context
    private lateinit var mockConfiguration: ConfigurationData
    private lateinit var mockPaymentMethodCheckerRegistry: PaymentMethodCheckerRegistry
    private lateinit var mockPaymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry
    private lateinit var mockPaymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry
    private lateinit var mockVaultedPaymentMethodProviderFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry
    private lateinit var mockPaymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry
    private lateinit var mockSdkContainer: SdkContainer
    private lateinit var mockErrorMapperRegistry: ErrorMapperRegistry
    private lateinit var mockBrandRegistry: BrandRegistry

    @BeforeEach
    fun setUp() {
        phoneNumber = PhoneNumber(paymentMethodType)
        mockContext = mockk()
        mockConfiguration = mockk()
        mockPaymentMethodCheckerRegistry = mockk(relaxed = true)
        mockPaymentMethodDescriptorFactoryRegistry = mockk(relaxed = true)
        mockPaymentMethodProviderFactoryRegistry = mockk(relaxed = true)
        mockVaultedPaymentMethodProviderFactoryRegistry = mockk(relaxed = true)
        mockPaymentMethodNavigationFactoryRegistry = mockk(relaxed = true)
        mockSdkContainer = mockk(relaxed = true)
        mockErrorMapperRegistry = mockk(relaxed = true)
        mockBrandRegistry = mockk(relaxed = true)
    }

    @Test
    fun `module initialize does not perform any operations`() {
        phoneNumber.module.initialize(mockContext, mockConfiguration)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodCheckers does not perform any operations`() {
        phoneNumber.module.registerPaymentMethodCheckers(mockPaymentMethodCheckerRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodDescriptorFactory registers PhoneNumberDescriptorFactory`() {
        phoneNumber.module.registerPaymentMethodDescriptorFactory(mockPaymentMethodDescriptorFactoryRegistry)

        verify {
            mockPaymentMethodDescriptorFactoryRegistry.register(
                paymentMethodType,
                any<PhoneNumberDescriptorFactory>(),
            )
        }
    }

    @Test
    fun `module registerPaymentMethodProviderFactory registers PhoneNumberComposerProviderFactory`() {
        phoneNumber.module.registerPaymentMethodProviderFactory(mockPaymentMethodProviderFactoryRegistry)

        verify {
            mockPaymentMethodProviderFactoryRegistry.register(
                paymentMethodType,
                PhoneNumberComposerProviderFactory::class.java,
            )
        }
    }

    @Test
    fun `module registerSavedPaymentMethodProviderFactory does not perform any operations`() {
        phoneNumber.module.registerSavedPaymentMethodProviderFactory(mockVaultedPaymentMethodProviderFactoryRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodNavigationFactory does not perform any operations`() {
        phoneNumber.module.registerPaymentMethodNavigationFactory(mockPaymentMethodNavigationFactoryRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerErrorMappers does not perform any operations`() {
        phoneNumber.module.registerErrorMappers(mockErrorMapperRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerBrandProvider registers XenditOvoBrand for XENDIT_OVO payment method type`() {
        val phoneNumber = PhoneNumber(PaymentMethodType.XENDIT_OVO.name)
        phoneNumber.module.registerBrandProvider(mockBrandRegistry)

        verify {
            mockBrandRegistry.register(PaymentMethodType.XENDIT_OVO.name, any<XenditOvoBrand>())
        }
        confirmVerified(mockBrandRegistry)
    }

    @Test
    fun `module registerBrandProvider registers MbWayBrand for ADYEN_MBWAY payment method type`() {
        val phoneNumber = PhoneNumber(PaymentMethodType.ADYEN_MBWAY.name)
        phoneNumber.module.registerBrandProvider(mockBrandRegistry)

        verify {
            mockBrandRegistry.register(PaymentMethodType.ADYEN_MBWAY.name, any<MbWayBrand>())
        }
        confirmVerified(mockBrandRegistry)
    }
}
