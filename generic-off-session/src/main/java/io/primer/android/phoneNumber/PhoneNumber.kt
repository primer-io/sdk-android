package io.primer.android.phoneNumber

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
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry
import io.primer.android.phoneMetadata.di.PhoneMetadataContainer
import io.primer.android.phoneNumber.di.PhoneNumberContainer
import io.primer.android.phoneNumber.implementation.composer.presentation.provider.PhoneNumberComposerProviderFactory
import io.primer.android.phoneNumber.implementation.composer.ui.assets.MbWayBrand
import io.primer.android.phoneNumber.implementation.composer.ui.assets.XenditOvoBrand

internal class PhoneNumber(internal val paymentMethodType: String) : PaymentMethod, DISdkComponent {

    override val type = paymentMethodType

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
                PhoneNumberDescriptorFactory()
            )
        }

        override fun registerPaymentMethodProviderFactory(
            paymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry
        ) {
            paymentMethodProviderFactoryRegistry.register(
                paymentMethodType = paymentMethodType,
                factory = PhoneNumberComposerProviderFactory::class.java
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
            // no-op
        }

        override fun registerDependencyContainer(sdkContainers: List<SdkContainer>) {
            sdkContainers.forEach { sdkContainer ->
                sdkContainer.registerContainer(PhoneNumberContainer({ getSdkContainer() }, paymentMethodType))
                sdkContainer.registerContainer(PhoneMetadataContainer { getSdkContainer() })
            }
        }

        override fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry) {
        }

        override fun registerBrandProvider(brandRegistry: BrandRegistry) {
            when (paymentMethodType) {
                PaymentMethodType.ADYEN_MBWAY.name -> {
                    brandRegistry.register(paymentMethodType = paymentMethodType, MbWayBrand())
                }
                PaymentMethodType.XENDIT_OVO.name -> {
                    brandRegistry.register(paymentMethodType = paymentMethodType, XenditOvoBrand())
                }
            }
        }
    }
}
