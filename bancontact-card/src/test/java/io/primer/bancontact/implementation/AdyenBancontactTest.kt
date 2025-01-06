package io.primer.bancontact.implementation

import android.content.Context
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.bancontact.AdyenBancontact
import io.primer.android.bancontact.AdyenBancontactPaymentMethodDescriptorFactory
import io.primer.android.bancontact.implementation.composer.presentation.provider.AdyenBancontactComposerProviderFactory
import io.primer.android.bancontact.implementation.composer.ui.assets.AdyenBancontactBrand
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AdyenBancontactTest {
    private val paymentMethodType = PaymentMethodType.ADYEN_BANCONTACT_CARD.name
    private lateinit var adyenBancontact: AdyenBancontact
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
        adyenBancontact = AdyenBancontact()
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
        adyenBancontact.module.initialize(mockContext, mockConfiguration)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodCheckers does not perform any operations`() {
        adyenBancontact.module.registerPaymentMethodCheckers(mockPaymentMethodCheckerRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodDescriptorFactory registers AdyenBancontactDescriptorFactory`() {
        adyenBancontact.module.registerPaymentMethodDescriptorFactory(mockPaymentMethodDescriptorFactoryRegistry)

        verify {
            mockPaymentMethodDescriptorFactoryRegistry.register(
                paymentMethodType,
                any<AdyenBancontactPaymentMethodDescriptorFactory>(),
            )
        }
    }

    @Test
    fun `module registerPaymentMethodProviderFactory registers AdyenBancontactComposerProviderFactory`() {
        adyenBancontact.module.registerPaymentMethodProviderFactory(mockPaymentMethodProviderFactoryRegistry)

        verify {
            mockPaymentMethodProviderFactoryRegistry.register(
                paymentMethodType,
                AdyenBancontactComposerProviderFactory::class.java,
            )
        }
    }

    @Test
    fun `module registerSavedPaymentMethodProviderFactory does not perform any operations`() {
        adyenBancontact.module.registerSavedPaymentMethodProviderFactory(
            mockVaultedPaymentMethodProviderFactoryRegistry,
        )
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodNavigationFactory does not perform any operations`() {
        adyenBancontact.module.registerPaymentMethodNavigationFactory(mockPaymentMethodNavigationFactoryRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerErrorMappers does not perform any operations`() {
        adyenBancontact.module.registerErrorMappers(mockErrorMapperRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerBrandProvider registers AdyenBancontactBrand`() {
        adyenBancontact.module.registerBrandProvider(mockBrandRegistry)

        verify {
            mockBrandRegistry.register(paymentMethodType, any<AdyenBancontactBrand>())
        }
    }
}
