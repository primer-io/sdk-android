package io.primer.android.components.di

import io.primer.android.components.DefaultPaymentMethodMapping
import io.primer.android.components.PaymentMethodListFactory
import io.primer.android.components.PaymentMethodMapping
import io.primer.android.components.implementation.core.paymentmethods.composer.registry.PrimerPaymentMethodComposerRegistry
import io.primer.android.components.implementation.core.paymentmethods.composer.registry.PrimerVaultedPaymentMethodComposerRegistry
import io.primer.android.components.implementation.data.PaymentMethodDescriptorsDataRepository
import io.primer.android.components.implementation.domain.PaymentMethodDescriptorsRepository
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.PrimerPaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.registry.PaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.composer.registry.VaultedPaymentMethodComposerRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry

internal class PaymentMethodDescriptorContainer(private val sdk: SdkContainer) :
    DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton<PaymentMethodCheckerRegistry> { PrimerPaymentMethodCheckerRegistry }

        registerSingleton { PaymentMethodDescriptorFactoryRegistry(resolve()) }

        registerSingleton<PaymentMethodMapping> {
            DefaultPaymentMethodMapping(
                sdk.resolve(),
                sdk.resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
            )
        }

        registerSingleton { PaymentMethodListFactory(resolve(), sdk.resolve()) }

        registerSingleton<PaymentMethodComposerRegistry> { PrimerPaymentMethodComposerRegistry() }

        registerSingleton<VaultedPaymentMethodComposerRegistry> { PrimerVaultedPaymentMethodComposerRegistry() }

        registerSingleton { PaymentMethodProviderFactoryRegistry() }

        registerSingleton { VaultedPaymentMethodProviderFactoryRegistry() }

        registerSingleton { PaymentMethodNavigationFactoryRegistry() }

        registerSingleton<PaymentMethodDescriptorsRepository> {
            PaymentMethodDescriptorsDataRepository(
                sdk.resolve(),
                sdk.resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
            )
        }
    }
}
