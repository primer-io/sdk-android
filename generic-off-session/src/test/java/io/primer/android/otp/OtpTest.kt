package io.primer.android.otp

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
import io.primer.android.otp.implementation.composer.presentation.provider.OtpComposerProviderFactory
import io.primer.android.otp.implementation.composer.ui.assets.AdyenBlikBrand
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OtpTest {
    private lateinit var otp: Otp
    private val paymentMethodType = "ADYEN_BLIK"

    @BeforeEach
    fun setup() {
        otp = Otp(paymentMethodType)
    }

    @Test
    fun `initialize doesn't use applicationContext or configuration when called`() {
        val mockContext = mockk<Context>()
        val mockConfiguration = mockk<ConfigurationData>()

        otp.module.initialize(mockContext, mockConfiguration)

        confirmVerified(mockContext, mockConfiguration)
    }

    @Test
    fun `registerPaymentMethodCheckers doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodCheckerRegistry>()

        otp.module.registerPaymentMethodCheckers(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodProviderFactory registers factory`() {
        val mockRegistry = mockk<PaymentMethodProviderFactoryRegistry>(relaxed = true)

        otp.module.registerPaymentMethodProviderFactory(mockRegistry)

        verify {
            mockRegistry.register(
                paymentMethodType = paymentMethodType,
                factory = OtpComposerProviderFactory::class.java,
            )
        }
        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerSavedPaymentMethodProviderFactory doesn't use registry when called`() {
        val mockRegistry = mockk<VaultedPaymentMethodProviderFactoryRegistry>()

        otp.module.registerSavedPaymentMethodProviderFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodNavigationFactory doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodNavigationFactoryRegistry>()

        otp.module.registerPaymentMethodNavigationFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodDescriptorFactory registers factory`() {
        val mockRegistry = mockk<PaymentMethodDescriptorFactoryRegistry>()
        every { mockRegistry.register(any(), any()) } just Runs

        otp.module.registerPaymentMethodDescriptorFactory(mockRegistry)

        verify {
            mockRegistry.register(
                type = otp.type,
                factory = any<OtpDescriptorFactory>(),
            )
        }
        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerErrorMappers doesn't use registry when called`() {
        val mockErrorMapperRegistry = mockk<ErrorMapperRegistry>()
        every { mockErrorMapperRegistry.register(any()) } just Runs

        otp.module.registerErrorMappers(mockErrorMapperRegistry)

        verify(exactly = 0) { mockErrorMapperRegistry.register(any()) }
        confirmVerified(mockErrorMapperRegistry)
    }

    @Test
    fun `registerBrandProvider registers AdyenBlikBrand`() {
        val mockBrandRegistry = mockk<BrandRegistry>()
        every { mockBrandRegistry.register(any(), any()) } just Runs

        otp.module.registerBrandProvider(mockBrandRegistry)

        verify {
            mockBrandRegistry.register(
                paymentMethodType = paymentMethodType,
                brand = any<AdyenBlikBrand>(),
            )
        }
        confirmVerified(mockBrandRegistry)
    }
}
