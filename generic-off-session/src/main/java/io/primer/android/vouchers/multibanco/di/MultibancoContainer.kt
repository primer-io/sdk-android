package io.primer.android.vouchers.multibanco.di

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.vouchers.multibanco.implementation.configuration.data.repository.MultibancoConfigurationDataRepository
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.DefaultMultibancoConfigurationInteractor
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.MultibancoConfigurationInteractor
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.model.MultibancoConfig
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.model.MultibancoConfigParams
import io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.data.MultibancoClientTokenParser
import io.primer.android.vouchers.multibanco.implementation.payment.resume.handler.MultibancoResumeHandler
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.datasource.MultibancoRemoteTokenizationDataSource
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.mapper.MultibancoTokenizationParamsMapper
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.model.MultibancoPaymentInstrumentDataRequest
import io.primer.android.vouchers.multibanco.implementation.tokenization.data.repository.MultibancoTokenizationDataRepository
import io.primer.android.vouchers.multibanco.implementation.tokenization.domain.DefaultMultibancoTokenizationInteractor
import io.primer.android.vouchers.multibanco.implementation.tokenization.domain.MultibancoTokenizationInteractor
import io.primer.android.vouchers.multibanco.implementation.tokenization.presentation.MultibancoTokenizationDelegate

internal class MultibancoContainer(private val sdk: () -> SdkContainer, private val paymentMethodType: String) :
    DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory(name = paymentMethodType) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                PrimerPaymentMethodManagerCategory.NATIVE_UI.name,
                analyticsInteractor = sdk().resolve()
            )
        }

        registerFactory<PaymentMethodConfigurationRepository<MultibancoConfig, MultibancoConfigParams>>(
            name = paymentMethodType
        ) {
            MultibancoConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve()
            )
        }

        registerFactory<MultibancoConfigurationInteractor>(name = paymentMethodType) {
            DefaultMultibancoConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory<BaseRemoteTokenizationDataSource<MultibancoPaymentInstrumentDataRequest>>(
            name = paymentMethodType
        ) {
            MultibancoRemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory { MultibancoTokenizationParamsMapper() }

        registerFactory<MultibancoTokenizationInteractor>(name = paymentMethodType) {
            DefaultMultibancoTokenizationInteractor(
                tokenizationRepository = MultibancoTokenizationDataRepository(
                    remoteTokenizationDataSource = sdk().resolve(paymentMethodType),
                    configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                    tokenizationParamsMapper = resolve()
                ),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerFactory {
            MultibancoTokenizationDelegate(
                configurationInteractor = resolve(name = paymentMethodType),
                tokenizationInteractor = resolve(name = paymentMethodType)
            )
        }

        registerFactory {
            MultibancoClientTokenParser()
        }

        registerFactory {
            MultibancoResumeHandler(
                clientTokenParser = resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve()
            )
        }
    }
}
