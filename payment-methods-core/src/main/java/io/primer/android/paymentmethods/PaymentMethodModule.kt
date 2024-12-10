package io.primer.android.paymentmethods

import android.content.Context
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry

/**
 * A **PaymentMethodModule** contains all the dependencies of a particular [PaymentMethod]. It can
 * be [initialized] if necessary and it can also register a [PaymentMethodChecker] with the
 * [PaymentMethodCheckerRegistry], as well as a [PaymentMethodDescriptorFactory] with the
 * [PaymentMethodDescriptorFactoryRegistry].
 */
interface PaymentMethodModule {

    /**
     * Initializes this payment method module, passing in all the information it may need to do
     * initialize itself.
     */
    fun initialize(
        applicationContext: Context,
        configuration: ConfigurationData
    )

    /**
     * To be called when the SDK is at its startup phase. Each [PaymentMethod] can declare its
     * [PaymentMethodModule] specifying (among other things) its [PaymentMethodChecker], that will
     * be run at an appropriate time in order to determine if it (the [PaymentMethod]) should be
     * made available or not, at run-time.
     * @see [PaymentMethod]
     * @see [PaymentMethodDescriptor]
     * @see [PaymentMethodCheckerRegistry]
     */
    fun registerPaymentMethodCheckers(
        paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry
    )

    /**
     * To be called when the SDK is at its startup phase. Each [PaymentMethod] can declare its
     * [PaymentMethodModule] specifying (among other things) its [PaymentMethodDescriptorFactory].
     * This [PaymentMethodDescriptorFactory] will be used to create a [PaymentMethodDescriptor].
     * when needed.
     * @see [PaymentMethod]
     * @see [PaymentMethodDescriptor]
     * @see [PaymentMethodDescriptorFactoryRegistry]
     */
    fun registerPaymentMethodDescriptorFactory(
        paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry
    )

    fun registerPaymentMethodProviderFactory(
        paymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry
    )

    fun registerSavedPaymentMethodProviderFactory(
        paymentMethodProviderFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry
    )

    fun registerPaymentMethodNavigationFactory(
        paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry
    )

    fun registerDependencyContainer(sdkContainers: List<SdkContainer>)

    fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry)

    fun registerBrandProvider(brandRegistry: BrandRegistry)
}
