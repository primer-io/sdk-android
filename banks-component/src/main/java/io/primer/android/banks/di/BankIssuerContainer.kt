package io.primer.android.banks.di

import io.primer.android.banks.implementation.configuration.data.repository.BankIssuerConfigurationDataRepository
import io.primer.android.banks.implementation.configuration.domain.BankIssuerConfigurationInteractor
import io.primer.android.banks.implementation.configuration.domain.DefaultBankIssuerConfigurationInteractor
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfig
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfigParams
import io.primer.android.banks.implementation.payment.presentation.delegate.presentation.BankIssuerPaymentDelegate
import io.primer.android.banks.implementation.payment.resume.clientToken.data.BankIssuerPaymentMethodClientTokenParser
import io.primer.android.banks.implementation.payment.resume.handler.BankIssuerResumeHandler
import io.primer.android.banks.implementation.rpc.data.models.IssuingBankDataRequest
import io.primer.android.banks.implementation.rpc.data.models.IssuingBankResultDataResponse
import io.primer.android.banks.implementation.rpc.presentation.delegate.GetBanksDelegate
import io.primer.android.banks.implementation.tokenization.data.datasource.BankIssuerRemoteTokenizationDataSource
import io.primer.android.banks.implementation.tokenization.data.mapper.BankIssuerTokenizationParamsMapper
import io.primer.android.banks.implementation.tokenization.data.model.BankIssuerPaymentInstrumentDataRequest
import io.primer.android.banks.implementation.tokenization.data.repository.BankIssuerTokenizationDataRepository
import io.primer.android.banks.implementation.tokenization.domain.BankIssuerTokenizationInteractor
import io.primer.android.banks.implementation.tokenization.domain.DefaultBankIssuerTokenizationInteractor
import io.primer.android.banks.implementation.tokenization.domain.model.BankIssuerPaymentInstrumentParams
import io.primer.android.banks.implementation.tokenization.presentation.BankIssuerTokenizationDelegate
import io.primer.android.clientSessionActions.di.ActionsContainer
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.utils.Constants
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.payments.di.PaymentsContainer
import io.primer.android.webRedirectShared.implementation.deeplink.data.repository.RedirectDeeplinkDataRepository
import io.primer.android.webRedirectShared.implementation.deeplink.domain.DefaultRedirectDeeplinkInteractor
import io.primer.android.webRedirectShared.implementation.deeplink.domain.RedirectDeeplinkInteractor
import io.primer.android.webRedirectShared.implementation.deeplink.domain.repository.RedirectDeeplinkRepository

internal class BankIssuerContainer(private val sdk: () -> SdkContainer, private val paymentMethodType: String) :
    DependencyContainer() {
    override fun registerInitialDependencies() {
        sdk().resolve<WhitelistedHttpBodyKeyProviderRegistry>().apply {
            listOf(
                IssuingBankDataRequest.provider,
                IssuingBankResultDataResponse.provider,
            ).forEach(::register)
        }

        registerFactory(name = paymentMethodType) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                    PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT.name,
                analyticsInteractor = sdk().resolve(),
            )
        }

        registerFactory(name = paymentMethodType) {
            SdkAnalyticsErrorLoggingDelegate(analyticsInteractor = sdk().resolve())
        }

        registerFactory(name = paymentMethodType) {
            SdkAnalyticsValidationErrorLoggingDelegate(analyticsInteractor = sdk().resolve())
        }

        registerFactory<RedirectDeeplinkRepository> {
            RedirectDeeplinkDataRepository(
                applicationIdProvider = sdk().resolve(Constants.APPLICATION_ID_PROVIDER_DI_KEY),
            )
        }

        registerFactory<RedirectDeeplinkInteractor>(name = paymentMethodType) {
            DefaultRedirectDeeplinkInteractor(deeplinkRepository = resolve())
        }

        registerFactory<PaymentMethodConfigurationRepository<BankIssuerConfig, BankIssuerConfigParams>>(
            name = paymentMethodType,
        ) {
            BankIssuerConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve(),
            )
        }

        registerFactory<BankIssuerConfigurationInteractor>(name = paymentMethodType) {
            DefaultBankIssuerConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory { BankIssuerTokenizationParamsMapper() }

        registerFactory<BaseRemoteTokenizationDataSource<BankIssuerPaymentInstrumentDataRequest>>(
            name = paymentMethodType,
        ) {
            BankIssuerRemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory<TokenizationRepository<BankIssuerPaymentInstrumentParams>>(name = paymentMethodType) {
            BankIssuerTokenizationDataRepository(
                remoteTokenizationDataSource = resolve(name = paymentMethodType),
                cacheDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                tokenizationParamsMapper = resolve(),
            )
        }

        registerFactory { BankIssuerPaymentMethodClientTokenParser() }

        registerFactory {
            BankIssuerResumeHandler(
                clientTokenParser = resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                configurationRepository = sdk().resolve(),
                deeplinkRepository = sdk().resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
            )
        }

        registerFactory<BankIssuerTokenizationInteractor>(name = paymentMethodType) {
            DefaultBankIssuerTokenizationInteractor(
                tokenizationRepository = resolve(name = paymentMethodType),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve(),
            )
        }

        registerFactory {
            GetBanksDelegate(
                paymentMethodType = paymentMethodType,
                banksInteractor = sdk().resolve(),
                banksFilterInteractor = sdk().resolve(),
                configurationInteractor = resolve(name = paymentMethodType),
            )
        }

        registerFactory(name = paymentMethodType) {
            BankIssuerTokenizationDelegate(
                configurationInteractor = resolve(name = paymentMethodType),
                primerSettings = sdk().resolve(),
                tokenizationInteractor = resolve(name = paymentMethodType),
                actionInteractor = sdk().resolve(ActionsContainer.ACTION_INTERACTOR_IGNORE_ERRORS_DI_KEY),
                deeplinkInteractor = resolve(name = paymentMethodType),
            )
        }

        registerSingleton(name = paymentMethodType) {
            BankIssuerPaymentDelegate(
                paymentMethodTokenHandler = sdk().resolve(),
                resumePaymentHandler = sdk().resolve(),
                successHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
                bankIssuerResumeHandler = resolve(),
            )
        }

        registerSingleton(name = paymentMethodType) {
            BankWebRedirectComposer(
                tokenizationDelegate = resolve(name = paymentMethodType),
                pollingInteractor = sdk().resolve(PaymentsContainer.POLLING_INTERACTOR_DI_KEY),
                paymentDelegate = resolve(name = paymentMethodType),
            )
        }
    }
}
