package io.primer.android.webredirect

import android.content.Context
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.webredirect.implementation.composer.presentation.provider.WebRedirectComposerProviderFactory
import io.primer.android.webredirect.implementation.composer.ui.assets.AlipayBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.AtomeBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.BanContactBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.EpsBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.GCashBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.GiropayBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.GrabPayBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.HoolahBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.InteracBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.MobilePayBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.OpenNodeBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.P24Brand
import io.primer.android.webredirect.implementation.composer.ui.assets.PayNowBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.PayShopBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.PayTrailBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.PayconiqBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.PoliBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.SofortBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.TrustlyBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.TwintBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.TwoC2PBrand
import io.primer.android.webredirect.implementation.composer.ui.assets.VippsBrand
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.provider.WebRedirectNavigatorProviderFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class WebRedirectPaymentMethodTest {

    private lateinit var webRedirectPaymentMethod: WebRedirectPaymentMethod
    private val paymentMethodType = "testPaymentMethodType"

    @BeforeEach
    fun setUp() {
        webRedirectPaymentMethod = WebRedirectPaymentMethod(paymentMethodType)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `type should return correct payment method type`() {
        assertEquals(paymentMethodType, webRedirectPaymentMethod.type)
    }

    @Test
    fun `canBeVaulted should return false`() {
        assertFalse(webRedirectPaymentMethod.canBeVaulted)
    }

    @Test
    fun `module should initialize without errors`() {
        val context = mockk<Context>()
        val configuration = mockk<ConfigurationData>()

        webRedirectPaymentMethod.module.initialize(context, configuration)
    }

    @Test
    fun `module should register payment method descriptor factory`() {
        val registry = mockk<PaymentMethodDescriptorFactoryRegistry>(relaxed = true)
        val localConfig = mockk<PrimerConfig>()
        val paymentMethodRemoteConfig = mockk<PaymentMethodConfigDataResponse>()
        val paymentMethodCheckers = mockk<PaymentMethodCheckerRegistry>()

        every { registry.register(any(), any()) } just Runs

        webRedirectPaymentMethod.module.registerPaymentMethodDescriptorFactory(registry)

        verify {
            registry.register(
                paymentMethodType,
                match {
                    it.create(
                        localConfig = localConfig,
                        paymentMethodRemoteConfig = paymentMethodRemoteConfig,
                        paymentMethod = webRedirectPaymentMethod,
                        paymentMethodCheckers = paymentMethodCheckers
                    ) is WebRedirectPaymentMethodDescriptor
                }
            )
        }
    }

    @Test
    fun `module should register payment method provider factory`() {
        val registry = mockk<PaymentMethodProviderFactoryRegistry>(relaxed = true)

        every { registry.register(any(), any()) } just Runs

        webRedirectPaymentMethod.module.registerPaymentMethodProviderFactory(registry)

        verify {
            registry.register(
                paymentMethodType,
                WebRedirectComposerProviderFactory::class.java
            )
        }
    }

    @Test
    fun `module should register payment method navigation factory`() {
        val registry = mockk<PaymentMethodNavigationFactoryRegistry>(relaxed = true)

        every { registry.register(any(), any()) } just Runs

        webRedirectPaymentMethod.module.registerPaymentMethodNavigationFactory(registry)

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

        webRedirectPaymentMethod.module.registerBrandProvider(brandRegistry)

        with(brandRegistry) {
            verify { register(PaymentMethodType.ADYEN_ALIPAY.name, ofType<AlipayBrand>()) }
            verify { register(PaymentMethodType.ATOME.name, ofType<AtomeBrand>()) }
            verify { register(PaymentMethodType.ADYEN_BANCONTACT_CARD.name, ofType<BanContactBrand>()) }
            verify { register(PaymentMethodType.BUCKAROO_BANCONTACT.name, ofType<BanContactBrand>()) }
            verify { register(PaymentMethodType.MOLLIE_BANCONTACT.name, ofType<BanContactBrand>()) }
            verify { register(PaymentMethodType.MOLLIE_EPS.name, ofType<EpsBrand>()) }
            verify { register(PaymentMethodType.BUCKAROO_EPS.name, ofType<EpsBrand>()) }
            verify { register(PaymentMethodType.PAY_NL_EPS.name, ofType<EpsBrand>()) }
            verify { register(PaymentMethodType.RAPYD_GCASH.name, ofType<GCashBrand>()) }
            verify { register(PaymentMethodType.ADYEN_GIROPAY.name, ofType<GiropayBrand>()) }
            verify { register(PaymentMethodType.BUCKAROO_GIROPAY.name, ofType<GiropayBrand>()) }
            verify { register(PaymentMethodType.PAY_NL_GIROPAY.name, ofType<GiropayBrand>()) }
            verify { register(PaymentMethodType.MOLLIE_GIROPAY.name, ofType<GiropayBrand>()) }
            verify { register(PaymentMethodType.RAPYD_GRABPAY.name, ofType<GrabPayBrand>()) }
            verify { register(PaymentMethodType.HOOLAH.name, ofType<HoolahBrand>()) }
            verify { register(PaymentMethodType.ADYEN_INTERAC.name, ofType<InteracBrand>()) }
            verify { register(PaymentMethodType.ADYEN_MOBILEPAY.name, ofType<MobilePayBrand>()) }
            verify { register(PaymentMethodType.OPENNODE.name, ofType<OpenNodeBrand>()) }
            verify { register(PaymentMethodType.PAY_NL_P24.name, ofType<P24Brand>()) }
            verify { register(PaymentMethodType.MOLLIE_P24.name, ofType<P24Brand>()) }
            verify { register(PaymentMethodType.XFERS_PAYNOW.name, ofType<PayNowBrand>()) }
            verify { register(PaymentMethodType.ADYEN_PAYSHOP.name, ofType<PayShopBrand>()) }
            verify { register(PaymentMethodType.ADYEN_PAYTRAIL.name, ofType<PayTrailBrand>()) }
            verify { register(PaymentMethodType.PAY_NL_PAYCONIQ.name, ofType<PayconiqBrand>()) }
            verify { register(PaymentMethodType.RAPYD_POLI.name, ofType<PoliBrand>()) }
            verify { register(PaymentMethodType.ADYEN_SOFORT.name, ofType<SofortBrand>()) }
            verify { register(PaymentMethodType.BUCKAROO_SOFORT.name, ofType<SofortBrand>()) }
            verify { register(PaymentMethodType.PRIMER_TEST_SOFORT.name, ofType<SofortBrand>()) }
            verify { register(PaymentMethodType.ADYEN_TRUSTLY.name, ofType<TrustlyBrand>()) }
            verify { register(PaymentMethodType.ADYEN_TWINT.name, ofType<TwintBrand>()) }
            verify { register(PaymentMethodType.TWOC2P.name, ofType<TwoC2PBrand>()) }
            verify { register(PaymentMethodType.ADYEN_VIPPS.name, ofType<VippsBrand>()) }
        }
    }
}
