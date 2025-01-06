package io.primer.android.vouchers.multibanco

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
import io.primer.android.vouchers.multibanco.implementation.composer.presentation.provider.MultibancoComposerProviderFactory
import io.primer.android.vouchers.multibanco.implementation.composer.ui.assets.MultibancoBrand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MultibancoTest {
    private val paymentMethodType = "multibanco"
    private lateinit var multibanco: Multibanco
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
        multibanco = Multibanco(paymentMethodType)
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
        multibanco.module.initialize(mockContext, mockConfiguration)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodCheckers does not perform any operations`() {
        multibanco.module.registerPaymentMethodCheckers(mockPaymentMethodCheckerRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodDescriptorFactory registers MultibancoDescriptorFactory`() {
        multibanco.module.registerPaymentMethodDescriptorFactory(mockPaymentMethodDescriptorFactoryRegistry)

        verify {
            mockPaymentMethodDescriptorFactoryRegistry.register(
                paymentMethodType,
                any<MultibancoDescriptorFactory>(),
            )
        }
    }

    @Test
    fun `module registerPaymentMethodProviderFactory registers MultibancoComposerProviderFactory`() {
        multibanco.module.registerPaymentMethodProviderFactory(mockPaymentMethodProviderFactoryRegistry)

        verify {
            mockPaymentMethodProviderFactoryRegistry.register(
                paymentMethodType,
                MultibancoComposerProviderFactory::class.java,
            )
        }
    }

    @Test
    fun `module registerSavedPaymentMethodProviderFactory does not perform any operations`() {
        multibanco.module.registerSavedPaymentMethodProviderFactory(mockVaultedPaymentMethodProviderFactoryRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerPaymentMethodNavigationFactory does not perform any operations`() {
        multibanco.module.registerPaymentMethodNavigationFactory(mockPaymentMethodNavigationFactoryRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerErrorMappers does not perform any operations`() {
        multibanco.module.registerErrorMappers(mockErrorMapperRegistry)
        // no specific behavior to verify
    }

    @Test
    fun `module registerBrandProvider registers MultibancoBrand`() {
        multibanco.module.registerBrandProvider(mockBrandRegistry)

        verify {
            mockBrandRegistry.register(paymentMethodType, any<MultibancoBrand>())
        }
    }
}
