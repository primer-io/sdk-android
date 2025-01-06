package io.primer.android.banks

import android.content.Context
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.banks.di.BankIssuerContainer
import io.primer.android.banks.implementation.composer.provider.BankIssuerComposerProviderFactory
import io.primer.android.banks.implementation.composer.ui.assets.DotpayBrand
import io.primer.android.banks.implementation.composer.ui.assets.IdealBrand
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

internal class BankIssuerPaymentMethod(internal val paymentMethodType: String) :
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
                            return BankIssuerPaymentMethodDescriptor(
                                paymentMethod as BankIssuerPaymentMethod,
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
                    paymentMethodType = paymentMethodType,
                    BankIssuerComposerProviderFactory::class.java,
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
                            BankIssuerContainer(
                                sdk = { getSdkContainer() },
                                paymentMethodType = paymentMethodType,
                            ),
                    )
                }
            }

            override fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry) {
                // no-op
            }

            override fun registerBrandProvider(brandRegistry: BrandRegistry) {
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_DOTPAY.name, DotpayBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.ADYEN_IDEAL.name, IdealBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.BUCKAROO_IDEAL.name, IdealBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.MOLLIE_IDEAL.name, IdealBrand())
                brandRegistry.register(paymentMethodType = PaymentMethodType.PAY_NL_IDEAL.name, IdealBrand())
            }
        }
}
