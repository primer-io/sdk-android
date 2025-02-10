package io.primer.android.card

import android.content.Context
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.card.di.CardContainer
import io.primer.android.card.implementation.composer.presentation.provider.CardComposerProviderFactory
import io.primer.android.card.implementation.composer.presentation.provider.VaultedCardComposerFactory
import io.primer.android.card.implementation.composer.ui.assets.CardBrand
import io.primer.android.card.implementation.composer.ui.navigation.provider.CardNavigatorProviderFactory
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.PaymentMethodModule
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry

internal class Card : PaymentMethod, DISdkComponent {
    override val type = PaymentMethodType.PAYMENT_CARD.name

    override val canBeVaulted: Boolean = true

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
                    CardPaymentMethodDescriptorFactory(),
                )
            }

            override fun registerPaymentMethodProviderFactory(
                paymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry,
            ) {
                paymentMethodProviderFactoryRegistry.register(
                    PaymentMethodType.PAYMENT_CARD.name,
                    CardComposerProviderFactory::class.java,
                )
            }

            override fun registerSavedPaymentMethodProviderFactory(
                paymentMethodProviderFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry,
            ) {
                paymentMethodProviderFactoryRegistry.register(
                    PaymentMethodType.PAYMENT_CARD.name,
                    VaultedCardComposerFactory::class.java,
                )
            }

            override fun registerPaymentMethodNavigationFactory(
                paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry,
            ) {
                paymentMethodNavigationFactoryRegistry.register(type, CardNavigatorProviderFactory::class.java)
            }

            override fun registerDependencyContainer(sdkContainers: List<SdkContainer>) {
                sdkContainers.forEach { sdkContainer ->
                    sdkContainer.registerContainer(
                        CardContainer(
                            sdk = { getSdkContainer() },
                            paymentMethodType = PaymentMethodType.PAYMENT_CARD.name,
                        ),
                    )
                }
            }

            override fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry) {
            }

            override fun registerBrandProvider(brandRegistry: BrandRegistry) {
                brandRegistry.register(paymentMethodType = PaymentMethodType.PAYMENT_CARD.name, CardBrand())
            }
        }
}
