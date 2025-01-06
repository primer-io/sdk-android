package io.primer.android.qrcode

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
import io.primer.android.qrcode.di.QrCodeContainer
import io.primer.android.qrcode.implementation.composer.presentation.provider.QrCodeComposerProviderFactory
import io.primer.android.qrcode.implementation.composer.ui.assets.PromptPayBrand

internal class QrCode(internal val paymentMethodType: String) : PaymentMethod, DISdkComponent {
    override val type = paymentMethodType

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
                paymentMethodDescriptorFactoryRegistry.register(
                    type,
                    QrCodeDescriptorFactory(),
                )
            }

            override fun registerPaymentMethodProviderFactory(
                paymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry,
            ) {
                paymentMethodProviderFactoryRegistry.register(
                    paymentMethodType = paymentMethodType,
                    factory = QrCodeComposerProviderFactory::class.java,
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
                // no-op
            }

            override fun registerDependencyContainer(sdkContainers: List<SdkContainer>) {
                sdkContainers.forEach { sdkContainer ->
                    sdkContainer.registerContainer(QrCodeContainer({ getSdkContainer() }, paymentMethodType))
                }
            }

            override fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry) {
            }

            override fun registerBrandProvider(brandRegistry: BrandRegistry) {
                brandRegistry.register(paymentMethodType = paymentMethodType, PromptPayBrand())
            }
        }
}
