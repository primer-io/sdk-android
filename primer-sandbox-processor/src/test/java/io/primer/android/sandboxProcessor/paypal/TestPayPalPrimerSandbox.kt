package io.primer.android.sandboxProcessor.paypal

import android.content.Context
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.sandboxProcessor.implementation.components.ui.assets.SandboxProcessorPayPalBrand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestPayPalPrimerSandbox {
    private lateinit var primerSandboxPayPal: SandboxProcessorPayPal
    private val paymentMethodType = PaymentMethodType.PRIMER_TEST_PAYPAL.name

    @BeforeEach
    fun setup() {
        primerSandboxPayPal = SandboxProcessorPayPal(paymentMethodType)
    }

    @Test
    fun `initialize doesn't use applicationContext or configuration when called`() {
        val mockContext = mockk<Context>()
        val mockConfiguration = mockk<ConfigurationData>()

        primerSandboxPayPal.module.initialize(mockContext, mockConfiguration)

        confirmVerified(mockContext, mockConfiguration)
    }

    @Test
    fun `registerPaymentMethodCheckers doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodCheckerRegistry>()

        primerSandboxPayPal.module.registerPaymentMethodCheckers(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodProviderFactory doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodProviderFactoryRegistry>(relaxed = true)

        primerSandboxPayPal.module.registerPaymentMethodProviderFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerSavedPaymentMethodProviderFactory doesn't use registry when called`() {
        val mockRegistry = mockk<VaultedPaymentMethodProviderFactoryRegistry>()

        primerSandboxPayPal.module.registerSavedPaymentMethodProviderFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodNavigationFactory doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodNavigationFactoryRegistry>()

        primerSandboxPayPal.module.registerPaymentMethodNavigationFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodDescriptorFactory registers factory`() {
        val mockRegistry = mockk<PaymentMethodDescriptorFactoryRegistry>()
        every { mockRegistry.register(any(), any()) } just Runs

        primerSandboxPayPal.module.registerPaymentMethodDescriptorFactory(mockRegistry)

        verify {
            mockRegistry.register(
                type = primerSandboxPayPal.type,
                factory = any<SandboxProcessorPayPalDescriptorFactory>(),
            )
        }
        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerErrorMappers doesn't use registry when called`() {
        val mockErrorMapperRegistry = mockk<ErrorMapperRegistry>()
        every { mockErrorMapperRegistry.register(any()) } just Runs

        primerSandboxPayPal.module.registerErrorMappers(mockErrorMapperRegistry)

        verify(exactly = 0) { mockErrorMapperRegistry.register(any()) }
        confirmVerified(mockErrorMapperRegistry)
    }

    @Test
    fun `registerBrandProvider registers TestPayPalBrand`() {
        val mockBrandRegistry = mockk<BrandRegistry>()
        every { mockBrandRegistry.register(any(), any()) } just Runs

        primerSandboxPayPal.module.registerBrandProvider(mockBrandRegistry)

        verify {
            mockBrandRegistry.register(
                paymentMethodType = paymentMethodType,
                brand = any<SandboxProcessorPayPalBrand>(),
            )
        }
        confirmVerified(mockBrandRegistry)
    }
}
