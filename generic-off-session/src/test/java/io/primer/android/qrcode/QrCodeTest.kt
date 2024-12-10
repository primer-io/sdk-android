package io.primer.android.qrcode

import android.content.Context
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.qrcode.implementation.composer.presentation.provider.QrCodeComposerProviderFactory
import io.primer.android.qrcode.implementation.composer.ui.assets.PromptPayBrand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class QrCodeTest {

    private val paymentMethodType = "qrCode"
    private lateinit var qrCode: QrCode
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
        qrCode = QrCode(paymentMethodType)
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
        qrCode.module.initialize(mockContext, mockConfiguration)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodCheckers does not perform any operations`() {
        qrCode.module.registerPaymentMethodCheckers(mockPaymentMethodCheckerRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodDescriptorFactory registers QrCodeDescriptorFactory`() {
        qrCode.module.registerPaymentMethodDescriptorFactory(mockPaymentMethodDescriptorFactoryRegistry)

        verify {
            mockPaymentMethodDescriptorFactoryRegistry.register(
                paymentMethodType,
                any<QrCodeDescriptorFactory>()
            )
        }
    }

    @Test
    fun `module registerPaymentMethodProviderFactory registers QrCodeComposerProviderFactory`() {
        qrCode.module.registerPaymentMethodProviderFactory(mockPaymentMethodProviderFactoryRegistry)

        verify {
            mockPaymentMethodProviderFactoryRegistry.register(
                paymentMethodType,
                QrCodeComposerProviderFactory::class.java
            )
        }
    }

    @Test
    fun `module registerSavedPaymentMethodProviderFactory does not perform any operations`() {
        qrCode.module.registerSavedPaymentMethodProviderFactory(mockVaultedPaymentMethodProviderFactoryRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodNavigationFactory does not perform any operations`() {
        qrCode.module.registerPaymentMethodNavigationFactory(mockPaymentMethodNavigationFactoryRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerErrorMappers does not perform any operations`() {
        qrCode.module.registerErrorMappers(mockErrorMapperRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerBrandProvider registers PromptPayBrand`() {
        qrCode.module.registerBrandProvider(mockBrandRegistry)

        verify {
            mockBrandRegistry.register(paymentMethodType, any<PromptPayBrand>())
        }
    }
}
