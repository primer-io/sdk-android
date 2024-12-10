package io.primer.android.sandboxProcessor.klarna

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
import io.primer.android.sandboxProcessor.implementation.components.ui.assets.SandboxProcessorKlarnaBrand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestKlarnaSandboxProcessor {
    private lateinit var testKlarna: SandboxProcessorKlarna
    private val paymentMethodType = PaymentMethodType.PRIMER_TEST_KLARNA.name

    @BeforeEach
    fun setup() {
        testKlarna = SandboxProcessorKlarna(paymentMethodType)
    }

    @Test
    fun `initialize doesn't use applicationContext or configuration when called`() {
        val mockContext = mockk<Context>()
        val mockConfiguration = mockk<ConfigurationData>()

        testKlarna.module.initialize(mockContext, mockConfiguration)

        confirmVerified(mockContext, mockConfiguration)
    }

    @Test
    fun `registerPaymentMethodCheckers doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodCheckerRegistry>()

        testKlarna.module.registerPaymentMethodCheckers(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodProviderFactory doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodProviderFactoryRegistry>(relaxed = true)

        testKlarna.module.registerPaymentMethodProviderFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerSavedPaymentMethodProviderFactory doesn't use registry when called`() {
        val mockRegistry = mockk<VaultedPaymentMethodProviderFactoryRegistry>()

        testKlarna.module.registerSavedPaymentMethodProviderFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodNavigationFactory doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodNavigationFactoryRegistry>()

        testKlarna.module.registerPaymentMethodNavigationFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodDescriptorFactory registers factory`() {
        val mockRegistry = mockk<PaymentMethodDescriptorFactoryRegistry>()
        every { mockRegistry.register(any(), any()) } just Runs

        testKlarna.module.registerPaymentMethodDescriptorFactory(mockRegistry)

        verify {
            mockRegistry.register(
                type = testKlarna.type,
                factory = any<SandboxProcessorKlarnaDescriptorFactory>()
            )
        }
        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerErrorMappers doesn't use registry when called`() {
        val mockErrorMapperRegistry = mockk<ErrorMapperRegistry>()
        every { mockErrorMapperRegistry.register(any()) } just Runs

        testKlarna.module.registerErrorMappers(mockErrorMapperRegistry)

        verify(exactly = 0) { mockErrorMapperRegistry.register(any()) }
        confirmVerified(mockErrorMapperRegistry)
    }

    @Test
    fun `registerBrandProvider registers TestKlarnaBrand`() {
        val mockBrandRegistry = mockk<BrandRegistry>()
        every { mockBrandRegistry.register(any(), any()) } just Runs

        testKlarna.module.registerBrandProvider(mockBrandRegistry)

        verify {
            mockBrandRegistry.register(
                paymentMethodType = paymentMethodType,
                brand = any<SandboxProcessorKlarnaBrand>()
            )
        }
        confirmVerified(mockBrandRegistry)
    }
}
