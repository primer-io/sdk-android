package io.primer.android.klarna.di

import io.primer.android.clientSessionActions.di.ActionsContainer
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.klarna.implementation.payment.presentation.KlarnaPaymentDelegate
import io.primer.android.klarna.implementation.session.data.datasource.RemoteFinalizeKlarnaSessionDataSource
import io.primer.android.klarna.implementation.session.data.datasource.RemoteKlarnaCheckoutPaymentSessionDataSource
import io.primer.android.klarna.implementation.session.data.datasource.RemoteKlarnaCustomerTokenDataSource
import io.primer.android.klarna.implementation.session.data.datasource.RemoteKlarnaVaultPaymentSessionDataSource
import io.primer.android.klarna.implementation.session.data.models.CreateCheckoutPaymentSessionDataRequest
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataRequest
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataResponse
import io.primer.android.klarna.implementation.session.data.models.CreateSessionDataResponse
import io.primer.android.klarna.implementation.session.data.models.CreateVaultPaymentSessionDataRequest
import io.primer.android.klarna.implementation.session.data.repository.FinalizeKlarnaSessionDataRepository
import io.primer.android.klarna.implementation.session.data.repository.KlarnaCustomerTokenDataRepository
import io.primer.android.klarna.implementation.session.data.repository.KlarnaSessionDataRepository
import io.primer.android.klarna.implementation.session.domain.FinalizeKlarnaSessionInteractor
import io.primer.android.klarna.implementation.session.domain.KlarnaCustomerTokenInteractor
import io.primer.android.klarna.implementation.session.domain.KlarnaSessionInteractor
import io.primer.android.klarna.implementation.session.domain.repository.FinalizeKlarnaSessionRepository
import io.primer.android.klarna.implementation.session.domain.repository.KlarnaCustomerTokenRepository
import io.primer.android.klarna.implementation.session.domain.repository.KlarnaSessionRepository
import io.primer.android.klarna.implementation.session.presentation.GetKlarnaAuthorizationSessionDataDelegate
import io.primer.android.klarna.implementation.session.presentation.KlarnaSessionCreationDelegate
import io.primer.android.klarna.implementation.tokenization.data.datasource.KlarnaPayRemoteTokenizationDataSource
import io.primer.android.klarna.implementation.tokenization.data.mapper.KlarnaTokenizationParamsMapper
import io.primer.android.klarna.implementation.tokenization.data.model.KlarnaPaymentInstrumentDataRequest
import io.primer.android.klarna.implementation.tokenization.data.repository.KlarnaTokenizationDataRepository
import io.primer.android.klarna.implementation.tokenization.domain.KlarnaTokenizationInteractor
import io.primer.android.klarna.implementation.tokenization.presentation.KlarnaTokenizationDelegate
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource

internal class KlarnaContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {
    override fun registerInitialDependencies() {
        sdk().resolve<WhitelistedHttpBodyKeyProviderRegistry>().apply {
            listOf(
                CreateVaultPaymentSessionDataRequest.provider,
                CreateCheckoutPaymentSessionDataRequest.provider,
                CreateSessionDataResponse.provider,
                CreateCustomerTokenDataRequest.provider,
                CreateCustomerTokenDataResponse.provider,
            ).forEach(::register)
        }

        registerFactory(name = PaymentMethodType.KLARNA.name) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.KLARNA.name,
                analyticsInteractor = sdk().resolve(),
            )
        }

        registerFactory(name = PaymentMethodType.KLARNA.name) {
            SdkAnalyticsErrorLoggingDelegate(analyticsInteractor = sdk().resolve())
        }

        registerFactory(name = PaymentMethodType.KLARNA.name) {
            SdkAnalyticsValidationErrorLoggingDelegate(analyticsInteractor = sdk().resolve())
        }

        registerSingleton { RemoteKlarnaCheckoutPaymentSessionDataSource(primerHttpClient = sdk().resolve()) }

        registerSingleton { RemoteKlarnaVaultPaymentSessionDataSource(primerHttpClient = sdk().resolve()) }

        registerSingleton { RemoteKlarnaCustomerTokenDataSource(primerHttpClient = sdk().resolve()) }

        registerSingleton { RemoteFinalizeKlarnaSessionDataSource(primerHttpClient = sdk().resolve()) }

        registerSingleton<KlarnaSessionRepository> {
            KlarnaSessionDataRepository(
                klarnaCheckoutPaymentSessionDataSource = resolve(),
                klarnaVaultPaymentSessionDataSource = sdk().resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                config = sdk().resolve(),
            )
        }

        registerSingleton<KlarnaCustomerTokenRepository> {
            KlarnaCustomerTokenDataRepository(
                remoteKlarnaCustomerTokenDataSource = resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                config = sdk().resolve(),
            )
        }

        registerSingleton<FinalizeKlarnaSessionRepository> {
            FinalizeKlarnaSessionDataRepository(
                remoteFinalizeKlarnaSessionDataSource = resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
            )
        }

        registerSingleton {
            KlarnaSessionInteractor(
                klarnaSessionRepository = resolve(),
            )
        }

        registerSingleton {
            KlarnaCustomerTokenInteractor(
                klarnaCustomerTokenRepository = resolve(),
            )
        }

        registerSingleton {
            FinalizeKlarnaSessionInteractor(
                finalizeKlarnaSessionRepository = resolve(),
            )
        }

        registerSingleton(name = PaymentMethodType.KLARNA.name) {
            KlarnaTokenizationDelegate(
                klarnaCustomerTokenInteractor = resolve(),
                finalizeKlarnaSessionInteractor = resolve(),
                tokenizationInteractor = sdk().resolve(),
            )
        }

        registerSingleton {
            KlarnaSessionCreationDelegate(
                actionInteractor = sdk().resolve(ActionsContainer.ACTION_INTERACTOR_IGNORE_ERRORS_DI_KEY),
                interactor = resolve(),
                primerSettings = sdk().resolve(),
                configurationInteractor = sdk().resolve(ConfigurationCoreContainer.CONFIGURATION_INTERACTOR_DI_KEY),
            )
        }

        registerSingleton {
            GetKlarnaAuthorizationSessionDataDelegate(
                configurationRepository = sdk().resolve(),
            )
        }

        registerFactory {
            KlarnaTokenizationParamsMapper()
        }

        registerFactory<BaseRemoteTokenizationDataSource<KlarnaPaymentInstrumentDataRequest>>(
            name = PaymentMethodType.KLARNA.name,
        ) {
            KlarnaPayRemoteTokenizationDataSource(
                primerHttpClient = sdk().resolve(),
                apiVersion = sdk().resolve<BaseDataProvider<PrimerApiVersion>>()::provide,
            )
        }

        registerFactory {
            KlarnaTokenizationInteractor(
                tokenizationRepository =
                    KlarnaTokenizationDataRepository(
                        sdk().resolve(dependencyName = PaymentMethodType.KLARNA.name),
                        sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                        resolve(),
                    ),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve(),
            )
        }

        registerFactory { KlarnaTokenizationDelegate(resolve(), resolve(), resolve()) }

        registerFactory(name = PaymentMethodType.KLARNA.name) {
            KlarnaPaymentDelegate(
                paymentMethodTokenHandler = sdk().resolve(),
                resumePaymentHandler = sdk().resolve(),
                successHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
            )
        }
    }
}
