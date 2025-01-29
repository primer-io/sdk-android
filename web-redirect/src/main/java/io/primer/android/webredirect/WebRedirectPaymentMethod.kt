package io.primer.android.webredirect

import android.content.Context
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.SdkContainer
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactory
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.PaymentMethodModule
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.provider.WebRedirectNavigatorProviderFactory
import io.primer.android.webredirect.di.WebRedirectContainer
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
import io.primer.android.webRedirectShared.di.WebRedirectContainer as SharedWebRedirectContainer

internal class WebRedirectPaymentMethod(
    private val paymentMethodType: String,
) :
    PaymentMethod, DISdkComponent {
    override val type = paymentMethodType
    override val canBeVaulted = false

    override val module: PaymentMethodModule =
        object : PaymentMethodModule {
            override fun initialize(
                applicationContext: Context,
                configuration: ConfigurationData,
            ) {
                // no-op
            }

            override fun registerPaymentMethodCheckers(paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry) {
                // no-op
            }

            override fun registerPaymentMethodDescriptorFactory(
                paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
            ) {
                paymentMethodDescriptorFactoryRegistry.register(
                    type,
                    object : PaymentMethodDescriptorFactory {
                        override fun create(
                            localConfig: PrimerConfig,
                            paymentMethodRemoteConfig: PaymentMethodConfigDataResponse,
                            paymentMethod: PaymentMethod,
                            paymentMethodCheckers: PaymentMethodCheckerRegistry,
                        ): PaymentMethodDescriptor {
                            return WebRedirectPaymentMethodDescriptor(
                                paymentMethod as WebRedirectPaymentMethod,
                                localConfig,
                                paymentMethodRemoteConfig,
                            )
                        }
                    },
                )
            }

            override fun registerPaymentMethodProviderFactory(
                paymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry,
            ) {
                paymentMethodProviderFactoryRegistry.register(
                    paymentMethodType,
                    WebRedirectComposerProviderFactory::class.java,
                )
            }

            override fun registerSavedPaymentMethodProviderFactory(
                paymentMethodProviderFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry,
            ) {
                // no-op
            }

            override fun registerPaymentMethodNavigationFactory(
                paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry,
            ) {
                paymentMethodNavigationFactoryRegistry.register(
                    paymentMethodType,
                    WebRedirectNavigatorProviderFactory::class.java,
                )
            }

            override fun registerDependencyContainer(sdkContainers: List<SdkContainer>) {
                sdkContainers.forEach { sdkContainer ->
                    sdkContainer.registerContainer(
                        name = paymentMethodType,
                        container =
                        WebRedirectContainer(
                            sdk = { getSdkContainer() },
                            paymentMethodType = paymentMethodType,
                        ),
                    )
                    // Register the shared container only once.
                    runCatching {
                        sdkContainer.resolve<SharedWebRedirectContainer>()
                    }
                        .onSuccess {
                            // no-op, container is already registered.
                        }
                        .onFailure {
                            sdkContainer.registerContainer(
                                container = SharedWebRedirectContainer(sdk = { getSdkContainer() }),
                            )
                        }
                }
            }

            override fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry) {
                // no-op
            }

            override fun registerBrandProvider(brandRegistry: BrandRegistry) {
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_ALIPAY.name, AlipayBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ATOME.name, AtomeBrand())
                brandRegistry.register(
                    paymentMethodType = PaymentMethodType.ADYEN_BANCONTACT_CARD.name,
                    BanContactBrand(),
                )
                brandRegistry.register(
                    paymentMethodType = PaymentMethodType.BUCKAROO_BANCONTACT.name,
                    BanContactBrand(),
                )
                brandRegistry.register(paymentMethodType = PaymentMethodType.MOLLIE_BANCONTACT.name, BanContactBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.PAY_NL_EPS.name, EpsBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.MOLLIE_EPS.name, EpsBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.BUCKAROO_EPS.name, EpsBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.RAPYD_GCASH.name, GCashBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_GIROPAY.name, GiropayBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.BUCKAROO_GIROPAY.name, GiropayBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.PAY_NL_GIROPAY.name, GiropayBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.MOLLIE_GIROPAY.name, GiropayBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.RAPYD_GRABPAY.name, GrabPayBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.HOOLAH.name, HoolahBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_INTERAC.name, InteracBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_MOBILEPAY.name, MobilePayBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.OPENNODE.name, OpenNodeBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.PAY_NL_P24.name, P24Brand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.MOLLIE_P24.name, P24Brand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.PAY_NL_PAYCONIQ.name, PayconiqBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.XFERS_PAYNOW.name, PayNowBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_PAYSHOP.name, PayShopBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_PAYTRAIL.name, PayTrailBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.RAPYD_POLI.name, PoliBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_SOFORT.name, SofortBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.BUCKAROO_SOFORT.name, SofortBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.PRIMER_TEST_SOFORT.name, SofortBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_TRUSTLY.name, TrustlyBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_TWINT.name, TwintBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.TWOC2P.name, TwoC2PBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_VIPPS.name, VippsBrand())
            }
        }
}
