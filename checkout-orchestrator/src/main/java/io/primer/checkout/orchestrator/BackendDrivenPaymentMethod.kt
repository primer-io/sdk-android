package io.primer.checkout.orchestrator

import android.content.Context
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.PaymentMethodModule
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.checkout.orchestrator.data.mapper.CheckoutOrchestratorErrorMapper
import io.primer.checkout.orchestrator.di.CheckoutOrchestratorContainer
import io.primer.checkout.orchestrator.presentation.BackendDrivenComposerProviderFactory
import io.primer.checkout.orchestrator.presentation.BackendDrivenNavigationHandlerFactory

internal class BackendDrivenPaymentMethod(
    override val type: String,
) : PaymentMethod, DISdkComponent {
    override val canBeVaulted = false

    override val module = object : PaymentMethodModule {
        override fun initialize(applicationContext: Context, configuration: ConfigurationData) = Unit

        override fun registerPaymentMethodCheckers(paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry) = Unit

        override fun registerPaymentMethodDescriptorFactory(
            paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
        ) {
            paymentMethodDescriptorFactoryRegistry.register(
                type,
                BackendDrivenPaymentMethodDescriptorFactory(),
            )
        }

        override fun registerPaymentMethodProviderFactory(
            paymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry,
        ) {
            paymentMethodProviderFactoryRegistry.register(
                type,
                BackendDrivenComposerProviderFactory::class.java,
            )
        }

        override fun registerSavedPaymentMethodProviderFactory(
            paymentMethodProviderFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry,
        ) = Unit

        override fun registerPaymentMethodNavigationFactory(
            paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry,
        ) {
            paymentMethodNavigationFactoryRegistry.register(
                type,
                BackendDrivenNavigationHandlerFactory::class.java,
            )
        }

        override fun registerDependencyContainer(sdkContainers: List<SdkContainer>) {
            sdkContainers.forEach { sdkContainer ->
                sdkContainer.registerContainer(CheckoutOrchestratorContainer(sdk = { getSdkContainer() }))
            }
        }

        override fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry) {
            errorMapperRegistry.register(CheckoutOrchestratorErrorMapper())
        }

        override fun registerBrandProvider(brandRegistry: BrandRegistry) = Unit
    }
}
