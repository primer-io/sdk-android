package io.primer.android.nolpay

import android.content.Context
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.nolpay.di.NolPayContainer
import io.primer.android.nolpay.implementation.errors.data.mapper.NolPayErrorMapper
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.PaymentMethodModule
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.phoneMetadata.di.PhoneMetadataContainer

internal class NolPay(override val type: String = PaymentMethodType.NOL_PAY.name) : PaymentMethod, DISdkComponent {
    override val canBeVaulted: Boolean = false
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
                paymentMethodDescriptorFactoryRegistry.register(type, NolPayDescriptorFactory())
            }

            override fun registerPaymentMethodProviderFactory(
                paymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry,
            ) {
                // no-op
            }

            override fun registerSavedPaymentMethodProviderFactory(
                paymentMethodProviderFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry,
            ) {
                // no-op
            }

            override fun registerPaymentMethodNavigationFactory(
                paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry,
            ) {
                // no-op
            }

            override fun registerDependencyContainer(sdkContainers: List<SdkContainer>) {
                sdkContainers.forEach { sdkContainer ->
                    sdkContainer.registerContainer(NolPayContainer { getSdkContainer() })
                    sdkContainer.registerContainer(PhoneMetadataContainer { getSdkContainer() })
                }
            }

            override fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry) {
                errorMapperRegistry.register(NolPayErrorMapper())
            }

            override fun registerBrandProvider(brandRegistry: BrandRegistry) {
                // no-op
            }
        }
}
