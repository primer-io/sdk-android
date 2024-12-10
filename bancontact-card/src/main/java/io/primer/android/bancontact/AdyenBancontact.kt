package io.primer.android.bancontact

import android.content.Context
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.bancontact.di.AdyenBancontactContainer
import io.primer.android.bancontact.implementation.composer.presentation.provider.AdyenBancontactComposerProviderFactory
import io.primer.android.bancontact.implementation.composer.ui.assets.AdyenBancontactBrand
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
import io.primer.android.webRedirectShared.di.WebRedirectContainer
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.provider.WebRedirectNavigatorProviderFactory

internal class AdyenBancontact : PaymentMethod, DISdkComponent {

    override val type = PaymentMethodType.ADYEN_BANCONTACT_CARD.name

    override val canBeVaulted: Boolean = false

    override val module: PaymentMethodModule = object : PaymentMethodModule {
        override fun initialize(applicationContext: Context, configuration: ConfigurationData) {
            // no-op
        }

        override fun registerPaymentMethodCheckers(
            paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry
        ) {
            // no-op
        }

        override fun registerPaymentMethodDescriptorFactory(
            paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry
        ) {
            paymentMethodDescriptorFactoryRegistry.register(
                type,
                AdyenBancontactPaymentMethodDescriptorFactory()
            )
        }

        override fun registerPaymentMethodProviderFactory(
            paymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry
        ) {
            paymentMethodProviderFactoryRegistry.register(
                PaymentMethodType.ADYEN_BANCONTACT_CARD.name,
                AdyenBancontactComposerProviderFactory::class.java
            )
        }

        override fun registerSavedPaymentMethodProviderFactory(
            paymentMethodProviderFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry
        ) {
            // no-op
        }

        override fun registerPaymentMethodNavigationFactory(
            paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry
        ) {
            paymentMethodNavigationFactoryRegistry.register(
                PaymentMethodType.ADYEN_BANCONTACT_CARD.name,
                WebRedirectNavigatorProviderFactory::class.java
            )
        }

        override fun registerDependencyContainer(sdkContainers: List<SdkContainer>) {
            sdkContainers.forEach { sdkContainer ->
                sdkContainer.registerContainer(WebRedirectContainer { getSdkContainer() })
                sdkContainer.registerContainer(
                    AdyenBancontactContainer(
                        sdk = { getSdkContainer() },
                        paymentMethodType = PaymentMethodType.ADYEN_BANCONTACT_CARD.name
                    )
                )
            }
        }

        override fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry) {
            // no-op
        }

        override fun registerBrandProvider(brandRegistry: BrandRegistry) {
            brandRegistry.register(
                paymentMethodType = PaymentMethodType.ADYEN_BANCONTACT_CARD.name,
                brand = AdyenBancontactBrand()
            )
        }
    }
}
