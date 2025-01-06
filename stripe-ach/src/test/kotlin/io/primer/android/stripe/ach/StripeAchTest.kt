package io.primer.android.stripe.ach

import android.content.Context
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
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
import io.primer.android.stripe.ach.implementation.composer.ui.assets.StripeAchBrand
import io.primer.android.stripe.ach.implementation.errors.data.mapper.StripeErrorMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class StripeAchTest {
    private lateinit var stripeAch: StripeAch

    @BeforeEach
    fun setup() {
        stripeAch = StripeAch()
    }

    @Test
    fun `initialize doesn't use applicationContext or configuration when called`() {
        val mockContext = mockk<Context>()
        val mockConfiguration = mockk<ConfigurationData>()

        stripeAch.module.initialize(mockContext, mockConfiguration)

        confirmVerified(mockContext, mockConfiguration)
    }

    @Test
    fun `registerPaymentMethodCheckers doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodCheckerRegistry>()

        stripeAch.module.registerPaymentMethodCheckers(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodProviderFactory doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodProviderFactoryRegistry>()

        stripeAch.module.registerPaymentMethodProviderFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerSavedPaymentMethodProviderFactory doesn't use registry when called`() {
        val mockRegistry = mockk<VaultedPaymentMethodProviderFactoryRegistry>()

        stripeAch.module.registerSavedPaymentMethodProviderFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodNavigationFactory doesn't use registry when called`() {
        val mockRegistry = mockk<PaymentMethodNavigationFactoryRegistry>()

        stripeAch.module.registerPaymentMethodNavigationFactory(mockRegistry)

        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerPaymentMethodDescriptorFactory registers factory`() {
        val mockRegistry = mockk<PaymentMethodDescriptorFactoryRegistry>()
        every { mockRegistry.register(any(), any()) } just Runs

        stripeAch.module.registerPaymentMethodDescriptorFactory(mockRegistry)

        verify {
            mockRegistry.register(
                type = stripeAch.type,
                factory = any<StripeAchPaymentMethodDescriptorFactory>(),
            )
        }
        confirmVerified(mockRegistry)
    }

    @Test
    fun `registerErrorMappers registers StripeErrorMapper`() {
        val mockErrorMapperRegistry = mockk<ErrorMapperRegistry>()
        every { mockErrorMapperRegistry.register(any()) } just Runs

        stripeAch.module.registerErrorMappers(mockErrorMapperRegistry)

        verify { mockErrorMapperRegistry.register(any<StripeErrorMapper>()) }
        confirmVerified(mockErrorMapperRegistry)
    }

    @Test
    fun `registerBrandProvider registers StripeAchBrand`() {
        val mockBrandRegistry = mockk<BrandRegistry>()
        every { mockBrandRegistry.register(any(), any()) } just Runs

        stripeAch.module.registerBrandProvider(mockBrandRegistry)

        verify {
            mockBrandRegistry.register(
                paymentMethodType = PaymentMethodType.STRIPE_ACH.name,
                brand = any<StripeAchBrand>(),
            )
        }
        confirmVerified(mockBrandRegistry)
    }
}
