package io.primer.android.components.implementation.data

import android.content.Context
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.components.PaymentMethodListFactory
import io.primer.android.components.implementation.domain.PaymentMethodDescriptorsRepository
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.PrimerPaymentMethodDescriptorResolver
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry

internal class PaymentMethodDescriptorsDataRepository(
    private val context: Context,
    private val configurationDataSource: CacheConfigurationDataSource,
    private val paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
    private val paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    private val paymentMethodListFactory: PaymentMethodListFactory,
    private val paymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry,
    private val vaultedPaymentMethodProviderFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry,
    private val paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry,
    private val errorMapperRegistry: ErrorMapperRegistry,
    private val brandRegistry: BrandRegistry,
    private val config: PrimerConfig
) : PaymentMethodDescriptorsRepository {

    private val descriptors = mutableListOf<PaymentMethodDescriptor>()

    override suspend fun resolvePaymentMethodDescriptors(): Result<List<PaymentMethodDescriptor>> = runSuspendCatching {
        configurationDataSource.get()
            .let { checkoutSession ->
                val paymentMethodDescriptorResolver = PrimerPaymentMethodDescriptorResolver(
                    config,
                    getPaymentMethods(checkoutSession),
                    paymentMethodDescriptorFactoryRegistry,
                    paymentMethodCheckerRegistry
                )

                paymentMethodDescriptorResolver.resolve(checkoutSession.paymentMethods).apply {
                    descriptors.addAll(this)
                }
            }
    }

    override fun getPaymentMethodDescriptors(): List<PaymentMethodDescriptor> {
        return descriptors
    }

    private fun getPaymentMethods(configuration: ConfigurationData): List<PaymentMethod> {
        val paymentMethods = paymentMethodListFactory.buildWith(configuration.paymentMethods)
        paymentMethods.forEach { paymentMethod ->
            initializeAndRegisterModules(context, paymentMethod, configuration)
        }

        return paymentMethods
    }

    private fun initializeAndRegisterModules(
        context: Context,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationData
    ) {
        if (config.paymentMethodIntent.isNotVault ||
            (config.paymentMethodIntent.isVault && paymentMethod.canBeVaulted) ||
            config.settings.fromHUC
        ) {
            paymentMethod.module.initialize(context, configuration)
            paymentMethod.module.registerPaymentMethodCheckers(paymentMethodCheckerRegistry)
            paymentMethod.module.registerPaymentMethodDescriptorFactory(
                paymentMethodDescriptorFactoryRegistry
            )
            paymentMethod.module.registerPaymentMethodProviderFactory(
                paymentMethodProviderFactoryRegistry
            )

            paymentMethod.module.registerSavedPaymentMethodProviderFactory(
                vaultedPaymentMethodProviderFactoryRegistry
            )

            paymentMethod.module.registerPaymentMethodNavigationFactory(
                paymentMethodNavigationFactoryRegistry
            )

            paymentMethod.module.registerDependencyContainer(
                sdkContainers = listOfNotNull(
                    DISdkContext.headlessSdkContainer,
                    DISdkContext.dropInSdkContainer
                )
            )

            paymentMethod.module.registerErrorMappers(errorMapperRegistry)

            paymentMethod.module.registerBrandProvider(brandRegistry)
        }
    }
}
