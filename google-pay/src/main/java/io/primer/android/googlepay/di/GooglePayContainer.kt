package io.primer.android.googlepay.di

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.googlepay.DefaultGooglePayFacadeFactory
import io.primer.android.googlepay.GooglePayFacadeFactory
import io.primer.android.googlepay.implementation.configuration.data.repository.GooglePayConfigurationDataRepository
import io.primer.android.googlepay.implementation.configuration.domain.GooglePayConfigurationInteractor
import io.primer.android.googlepay.implementation.configuration.domain.GooglePayConfigurationRepository
import io.primer.android.googlepay.implementation.payment.delegate.GooglePayPaymentDelegate
import io.primer.android.googlepay.implementation.payment.resume.clientToken.data.GooglePayClientTokenParser
import io.primer.android.googlepay.implementation.payment.resume.handler.GooglePayResumeHandler
import io.primer.android.googlepay.implementation.tokenization.data.datasource.GooglePayRemoteTokenizationDataSource
import io.primer.android.googlepay.implementation.tokenization.data.mapper.GooglePayTokenizationParamsMapper
import io.primer.android.googlepay.implementation.tokenization.data.model.GooglePayPaymentInstrumentDataRequest
import io.primer.android.googlepay.implementation.tokenization.data.repository.GooglePayTokenizationDataRepository
import io.primer.android.googlepay.implementation.tokenization.domain.DefaultGooglePayTokenizationInteractor
import io.primer.android.googlepay.implementation.tokenization.domain.GooglePayTokenizationInteractor
import io.primer.android.googlepay.implementation.tokenization.presentation.GooglePayTokenizationCollectorDelegate
import io.primer.android.googlepay.implementation.tokenization.presentation.GooglePayTokenizationDelegate
import io.primer.android.googlepay.implementation.validation.GooglePayShippingMethodUpdateValidator
import io.primer.android.googlepay.implementation.validation.GooglePayValidPaymentDataMethodRule
import io.primer.android.googlepay.implementation.validation.GooglePayValidationRulesResolver
import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource

internal class GooglePayContainer(private val sdk: () -> SdkContainer, private val paymentMethodType: String) :
    DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory<GooglePayConfigurationRepository>(
            name = paymentMethodType
        ) {
            GooglePayConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve()
            )
        }

        registerFactory<GooglePayConfigurationInteractor>(name = paymentMethodType) {
            PaymentMethodConfigurationInteractor(
                configurationRepository = resolve(name = paymentMethodType)
            )
        }

        registerFactory { GooglePayValidPaymentDataMethodRule() }

        registerFactory {
            GooglePayValidationRulesResolver(
                validPaymentDataMethodRule = resolve()
            )
        }

        registerSingleton {
            GooglePayShippingMethodUpdateValidator(
                configurationRepository = resolve(name = paymentMethodType)
            )
        }

        registerFactory<BaseRemoteTokenizationDataSource<GooglePayPaymentInstrumentDataRequest>>(
            name = paymentMethodType
        ) {
            GooglePayRemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory { GooglePayTokenizationParamsMapper() }

        registerFactory<GooglePayTokenizationInteractor>(name = paymentMethodType) {
            DefaultGooglePayTokenizationInteractor(
                tokenizationRepository = GooglePayTokenizationDataRepository(
                    remoteTokenizationDataSource = sdk().resolve(paymentMethodType),
                    configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                    tokenizationParamsMapper = resolve()
                ),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerFactory(name = paymentMethodType) {
            GooglePayTokenizationCollectorDelegate(
                configurationInteractor = resolve(
                    name = paymentMethodType
                )
            )
        }

        registerFactory(name = paymentMethodType) {
            GooglePayTokenizationDelegate(
                configurationInteractor = resolve(
                    name = paymentMethodType
                ),
                tokenizationInteractor = resolve(
                    name = paymentMethodType
                )
            )
        }

        registerFactory<GooglePayFacadeFactory> {
            DefaultGooglePayFacadeFactory()
        }

        registerFactory {
            GooglePayClientTokenParser()
        }

        registerFactory {
            GooglePayResumeHandler(
                clientTokenParser = resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve()
            )
        }

        registerFactory(name = paymentMethodType) {
            GooglePayPaymentDelegate(
                paymentMethodTokenHandler = sdk().resolve(),
                resumePaymentHandler = sdk().resolve(),
                successHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
                resumeHandler = resolve()
            )
        }
    }
}
