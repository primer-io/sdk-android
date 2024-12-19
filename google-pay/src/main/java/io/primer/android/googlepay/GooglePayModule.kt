package io.primer.android.googlepay

import android.content.Context
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.Environment
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.di.extensions.resolve
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.googlepay.di.GooglePayContainer
import io.primer.android.googlepay.implementation.composer.presentation.provider.GooglePayComposerProviderFactory
import io.primer.android.googlepay.implementation.composer.presentation.provider.VaultedGooglePayComposerFactory
import io.primer.android.googlepay.implementation.composer.ui.assets.GooglePayBrand
import io.primer.android.googlepay.implementation.composer.ui.navigation.provider.GooglePayNavigatorProviderFactory
import io.primer.android.googlepay.implementation.configuration.domain.GooglePayConfigurationRepository
import io.primer.android.googlepay.implementation.errors.data.mapper.GooglePayErrorMapper
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.paymentmethods.PaymentMethodModule
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.composer.provider.VaultedPaymentMethodProviderFactoryRegistry
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationFactoryRegistry

internal class GooglePayModule(
    private val googlePayFacadeFactory: GooglePayFacadeFactory = DefaultGooglePayFacadeFactory()
) : PaymentMethodModule, DISdkComponent {

    private lateinit var googlePayFacade: GooglePayFacade

    override fun initialize(applicationContext: Context, configuration: ConfigurationData) {
        val googlePayEnvironment = if (configuration.environment == Environment.PRODUCTION) {
            GooglePayFacade.Environment.PRODUCTION
        } else {
            GooglePayFacade.Environment.TEST
        }
        googlePayFacade =
            googlePayFacadeFactory.create(applicationContext, googlePayEnvironment, resolve())
    }

    override fun registerPaymentMethodCheckers(
        paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry
    ) {
        val googlePayChecker = GooglePayPaymentMethodChecker(googlePayFacade)

        paymentMethodCheckerRegistry.register(
            PaymentMethodType.GOOGLE_PAY.name,
            googlePayChecker
        )
    }

    override fun registerPaymentMethodDescriptorFactory(
        paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry
    ) {
        val paymentMethodDescriptorFactory =
            GooglePayPaymentMethodDescriptorFactory()

        paymentMethodDescriptorFactoryRegistry.register(
            PaymentMethodType.GOOGLE_PAY.name,
            paymentMethodDescriptorFactory
        )
    }

    override fun registerPaymentMethodProviderFactory(
        paymentMethodProviderFactoryRegistry: PaymentMethodProviderFactoryRegistry
    ) {
        paymentMethodProviderFactoryRegistry.register(
            PaymentMethodType.GOOGLE_PAY.name,
            GooglePayComposerProviderFactory::class.java
        )
    }

    override fun registerSavedPaymentMethodProviderFactory(
        paymentMethodProviderFactoryRegistry: VaultedPaymentMethodProviderFactoryRegistry
    ) {
        paymentMethodProviderFactoryRegistry.register(
            PaymentMethodType.GOOGLE_PAY.name,
            VaultedGooglePayComposerFactory::class.java
        )
    }

    override fun registerPaymentMethodNavigationFactory(
        paymentMethodNavigationFactoryRegistry: PaymentMethodNavigationFactoryRegistry
    ) {
        paymentMethodNavigationFactoryRegistry.register(
            PaymentMethodType.GOOGLE_PAY.name,
            GooglePayNavigatorProviderFactory::class.java
        )
    }

    override fun registerDependencyContainer(sdkContainers: List<SdkContainer>) {
        sdkContainers.forEach { sdkContainer ->
            sdkContainer.registerContainer(
                GooglePayContainer(
                    sdk = { getSdkContainer() },
                    paymentMethodType = PaymentMethodType.GOOGLE_PAY.name
                )
            )
        }
    }

    override fun registerErrorMappers(errorMapperRegistry: ErrorMapperRegistry) {
        errorMapperRegistry.register(GooglePayErrorMapper())
    }

    override fun registerBrandProvider(brandRegistry: BrandRegistry) {
        brandRegistry.register(
            paymentMethodType = PaymentMethodType.GOOGLE_PAY.name,
            brand = GooglePayBrand(
                resolve<PrimerSettings>().paymentMethodOptions.googlePayOptions,
                resolve<GooglePayConfigurationRepository>(name = PaymentMethodType.GOOGLE_PAY.name)
            )
        )
    }
}
