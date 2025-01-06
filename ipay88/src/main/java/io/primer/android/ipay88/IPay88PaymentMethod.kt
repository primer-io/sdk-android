package io.primer.android.ipay88

import android.content.Context
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.SdkContainer
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.ipay88.di.IPay88Container
import io.primer.android.ipay88.implementation.composer.presentation.provider.IPay88ComposerProviderFactory
import io.primer.android.ipay88.implementation.composer.ui.assets.IPay88Brand
import io.primer.android.ipay88.implementation.composer.ui.navigation.provider.IPay88NavigatorProviderFactory
import io.primer.android.ipay88.implementation.errors.data.mapper.IPayErrorMapper
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactory
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.PaymentMethodModule
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry

internal class IPay88PaymentMethod(private val paymentMethodType: String) :
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
                            return IPay88PaymentMethodDescriptor(
                                paymentMethod as IPay88PaymentMethod,
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
                    IPay88ComposerProviderFactory::class.java,
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
                    IPay88NavigatorProviderFactory::class.java,
                )
            }

            override fun registerDependencyContainer(sdkContainers: List<SdkContainer>) {
                sdkContainers.forEach { sdkContainer ->
                    sdkContainer.registerContainer(
                        name = paymentMethodType,
                        container =
                            IPay88Container(
                                sdk = { getSdkContainer() },
                                paymentMethodType = paymentMethodType,
                            ),
                    )
                }
            }

            override fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry) {
                errorMapperRegistry.register(IPayErrorMapper())
            }

            override fun registerBrandProvider(brandRegistry: BrandRegistry) {
                brandRegistry.register(paymentMethodType = paymentMethodType, brand = IPay88Brand())
            }
        }
}
