package io.primer.android.banks

import android.content.Context
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.banks.implementation.composer.provider.BankIssuerComposerProviderFactory
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.banks.implementation.composer.ui.assets.IdealBrand
import io.primer.android.banks.implementation.composer.ui.assets.DotpayBrand
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.provider.WebRedirectNavigatorProviderFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class BankIssuerPaymentMethodTest {

    private lateinit var bankIssuerPaymentMethod: BankIssuerPaymentMethod
    private val paymentMethodType = "testPaymentMethodType"

    @BeforeEach
    fun setUp() {
        bankIssuerPaymentMethod = BankIssuerPaymentMethod(paymentMethodType)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `type should return correct payment method type`() {
        assertEquals(paymentMethodType, bankIssuerPaymentMethod.type)
    }

    @Test
    fun `canBeVaulted should return false`() {
        assertFalse(bankIssuerPaymentMethod.canBeVaulted)
    }

    @Test
    fun `module should initialize without errors`() {
        val context = mockk<Context>()
        val configuration = mockk<ConfigurationData>()

        bankIssuerPaymentMethod.module.initialize(context, configuration)
    }

    @Test
    fun `module should register payment method descriptor factory`() {
        val registry = mockk<PaymentMethodDescriptorFactoryRegistry>(relaxed = true)
        val localConfig = mockk<PrimerConfig>()
        val paymentMethodRemoteConfig = mockk<PaymentMethodConfigDataResponse>()
        val paymentMethodCheckers = mockk<PaymentMethodCheckerRegistry>()

        every { registry.register(any(), any()) } just Runs

        bankIssuerPaymentMethod.module.registerPaymentMethodDescriptorFactory(registry)

        verify {
            registry.register(
                paymentMethodType,
                match {
                    it.create(
                        localConfig = localConfig,
                        paymentMethodRemoteConfig = paymentMethodRemoteConfig,
                        paymentMethod = bankIssuerPaymentMethod,
                        paymentMethodCheckers = paymentMethodCheckers
                    ) is BankIssuerPaymentMethodDescriptor
                }
            )
        }
    }

    @Test
    fun `module should register payment method provider factory`() {
        val registry = mockk<PaymentMethodProviderFactoryRegistry>(relaxed = true)

        every { registry.register(any(), any()) } just Runs

        bankIssuerPaymentMethod.module.registerPaymentMethodProviderFactory(registry)

        verify {
            registry.register(
                paymentMethodType,
                BankIssuerComposerProviderFactory::class.java
            )
        }
    }

    @Test
    fun `module should register payment method navigation factory`() {
        val registry = mockk<PaymentMethodNavigationFactoryRegistry>(relaxed = true)

        every { registry.register(any(), any()) } just Runs

        bankIssuerPaymentMethod.module.registerPaymentMethodNavigationFactory(registry)

        verify {
            registry.register(
                paymentMethodType,
                WebRedirectNavigatorProviderFactory::class.java
            )
        }
    }

    @Test
    fun `module should register brand provider`() {
        val brandRegistry = mockk<BrandRegistry>(relaxed = true)

        every { brandRegistry.register(any(), any()) } just Runs

        bankIssuerPaymentMethod.module.registerBrandProvider(brandRegistry)

        with(brandRegistry) {
            verify { register(PaymentMethodType.ADYEN_DOTPAY.name, ofType<DotpayBrand>()) }
            verify { register(PaymentMethodType.ADYEN_IDEAL.name, ofType<IdealBrand>()) }
            verify { register(PaymentMethodType.BUCKAROO_IDEAL.name, ofType<IdealBrand>()) }
            verify { register(PaymentMethodType.MOLLIE_IDEAL.name, ofType<IdealBrand>()) }
            verify { register(PaymentMethodType.PAY_NL_IDEAL.name, ofType<IdealBrand>()) }
        }
    }
}
