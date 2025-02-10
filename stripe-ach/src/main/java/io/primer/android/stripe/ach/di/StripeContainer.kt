package io.primer.android.stripe.ach.di

import android.content.Context
import io.primer.android.clientSessionActions.di.ActionsContainer
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.stripe.ach.api.mandate.delegate.GetStripeMandateDelegate
import io.primer.android.stripe.ach.implementation.configuration.data.repository.StripeAchConfigurationDataRepository
import io.primer.android.stripe.ach.implementation.configuration.domain.DefaultStripeAchConfigurationInteractor
import io.primer.android.stripe.ach.implementation.configuration.domain.StripeAchConfigurationInteractor
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfig
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfigParams
import io.primer.android.stripe.ach.implementation.mandate.presentation.StripeAchMandateTimestampLoggingDelegate
import io.primer.android.stripe.ach.implementation.payment.confirmation.data.datasource.RemoteStripeAchCompletePaymentDataSource
import io.primer.android.stripe.ach.implementation.payment.confirmation.data.repository.StripeAchCompletePaymentDataRepository
import io.primer.android.stripe.ach.implementation.payment.confirmation.domain.StripeAchCompletePaymentInteractor
import io.primer.android.stripe.ach.implementation.payment.confirmation.domain.repository.StripeAchCompletePaymentRepository
import io.primer.android.stripe.ach.implementation.payment.confirmation.presentation.CompleteStripeAchPaymentSessionDelegate
import io.primer.android.stripe.ach.implementation.payment.presentation.StripeAchPaymentDelegate
import io.primer.android.stripe.ach.implementation.payment.presentation.StripeAchVaultPaymentDelegate
import io.primer.android.stripe.ach.implementation.payment.resume.clientToken.data.StripeAchPaymentMethodClientTokenParser
import io.primer.android.stripe.ach.implementation.payment.resume.handler.StripeAchResumeDecisionHandler
import io.primer.android.stripe.ach.implementation.payment.resume.handler.StripeAchVaultResumeDecisionHandler
import io.primer.android.stripe.ach.implementation.selection.presentation.StripeAchBankFlowDelegate
import io.primer.android.stripe.ach.implementation.selection.presentation.StripeAchBankSelectionHandler
import io.primer.android.stripe.ach.implementation.session.presentation.GetClientSessionCustomerDetailsDelegate
import io.primer.android.stripe.ach.implementation.session.presentation.GetStripePublishableKeyDelegate
import io.primer.android.stripe.ach.implementation.session.presentation.StripeAchClientSessionPatchDelegate
import io.primer.android.stripe.ach.implementation.tokenization.data.datasource.StripeAchRemoteTokenizationDataSource
import io.primer.android.stripe.ach.implementation.tokenization.data.mapper.StripeAchTokenizationParamsMapper
import io.primer.android.stripe.ach.implementation.tokenization.data.model.StripeAchPaymentInstrumentDataRequest
import io.primer.android.stripe.ach.implementation.tokenization.data.repository.StripeAchTokenizationDataRepository
import io.primer.android.stripe.ach.implementation.tokenization.domain.StripeAchTokenizationInteractor
import io.primer.android.stripe.ach.implementation.tokenization.domain.model.StripeAchPaymentInstrumentParams
import io.primer.android.stripe.ach.implementation.tokenization.presentation.StripeAchTokenizationDelegate
import io.primer.android.stripe.ach.implementation.validation.resolvers.StripeInitValidationRulesResolver
import io.primer.android.stripe.ach.implementation.validation.rules.ValidStripeMandateDataRule
import io.primer.android.stripe.ach.implementation.validation.rules.ValidStripePublishableKeyRule

internal class StripeContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {
    @Suppress("LongMethod")
    override fun registerInitialDependencies() {
        val paymentMethodType = PaymentMethodType.STRIPE_ACH.name

        registerSingleton(name = paymentMethodType) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.STRIPE_ACH.name,
                analyticsInteractor = sdk().resolve(),
            )
        }

        registerSingleton {
            SdkAnalyticsErrorLoggingDelegate(analyticsInteractor = sdk().resolve())
        }

        registerFactory(name = paymentMethodType) {
            SdkAnalyticsValidationErrorLoggingDelegate(analyticsInteractor = sdk().resolve())
        }

        registerSingleton {
            GetClientSessionCustomerDetailsDelegate(
                configurationInteractor =
                sdk().resolve(
                    ConfigurationCoreContainer.CONFIGURATION_INTERACTOR_DI_KEY,
                ),
            )
        }

        registerSingleton {
            StripeAchClientSessionPatchDelegate(
                configurationInteractor =
                sdk().resolve(
                    ConfigurationCoreContainer.CONFIGURATION_INTERACTOR_DI_KEY,
                ),
                actionInteractor = sdk().resolve(ActionsContainer.ACTION_INTERACTOR_IGNORE_ERRORS_DI_KEY),
            )
        }

        registerSingleton<BaseRemoteTokenizationDataSource<StripeAchPaymentInstrumentDataRequest>> {
            StripeAchRemoteTokenizationDataSource(
                primerHttpClient = sdk().resolve(),
                apiVersion = sdk().resolve<BaseDataProvider<PrimerApiVersion>>()::provide,
            )
        }

        registerSingleton<
            TokenizationParamsMapper<StripeAchPaymentInstrumentParams, StripeAchPaymentInstrumentDataRequest>,
            > {
            StripeAchTokenizationParamsMapper()
        }

        registerSingleton<TokenizationRepository<StripeAchPaymentInstrumentParams>> {
            StripeAchTokenizationDataRepository(
                remoteTokenizationDataSource = resolve(),
                localConfigurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                tokenizationParamsMapper = resolve(),
            )
        }

        registerSingleton {
            StripeAchTokenizationInteractor(
                tokenizationRepository = resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve(),
            )
        }

        registerSingleton<PaymentMethodConfigurationRepository<StripeAchConfig, StripeAchConfigParams>> {
            StripeAchConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve(),
            )
        }

        registerSingleton<StripeAchConfigurationInteractor> {
            DefaultStripeAchConfigurationInteractor(configurationRepository = resolve())
        }

        registerSingleton {
            StripeAchTokenizationDelegate(
                stripeAchConfigurationInteractor = resolve(),
                tokenizationInteractor = resolve(),
                primerSettings = sdk().resolve(),
                actionInteractor = sdk().resolve(ActionsContainer.ACTION_INTERACTOR_IGNORE_ERRORS_DI_KEY),
            )
        }

        registerSingleton { RemoteStripeAchCompletePaymentDataSource(primerHttpClient = sdk().resolve()) }

        registerSingleton<StripeAchCompletePaymentRepository> {
            StripeAchCompletePaymentDataRepository(completePaymentDataSource = resolve())
        }

        registerSingleton { StripeAchPaymentMethodClientTokenParser() }

        registerSingleton {
            StripeAchBankSelectionHandler(
                contextProvider = { sdk().resolve<Context>() },
                checkoutAdditionalInfoHandler = sdk().resolve(),
                stripePublishableKeyDelegate = resolve(),
                getClientSessionCustomerDetailsDelegate = resolve(),
                mockConfigurationDelegate = sdk().resolve(),
            )
        }

        registerSingleton {
            StripeAchResumeDecisionHandler(
                clientTokenParser = resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
            )
        }

        registerSingleton {
            StripeAchVaultResumeDecisionHandler(
                clientTokenParser = resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
            )
        }

        registerSingleton {
            StripeAchVaultResumeDecisionHandler(
                clientTokenParser = resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
            )
        }

        registerFactory {
            StripeAchPaymentDelegate(
                paymentMethodTokenHandler = sdk().resolve(),
                resumePaymentHandler = sdk().resolve(),
                successHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
                resumeDecisionHandler = resolve(),
            )
        }

        registerFactory<PaymentMethodPaymentDelegate>(paymentMethodType) {
            StripeAchVaultPaymentDelegate(
                paymentMethodTokenHandler = sdk().resolve(),
                resumePaymentHandler = sdk().resolve(),
                successHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
                resumeDecisionHandler = resolve(),
                completeStripeAchPaymentSessionDelegate = resolve(),
                paymentResultRepository = sdk().resolve(),
                config = sdk().resolve(),
                pendingResumeHandler = sdk().resolve(),
                manualFlowSuccessHandler = sdk().resolve(),
            )
        }

        registerSingleton { StripeAchCompletePaymentInteractor(completePaymentRepository = resolve()) }

        registerSingleton { CompleteStripeAchPaymentSessionDelegate(stripeAchCompletePaymentInteractor = resolve()) }

        registerSingleton { GetStripePublishableKeyDelegate(primerSettings = sdk().resolve()) }

        registerSingleton {
            StripeAchMandateTimestampLoggingDelegate(
                logReporter = sdk().resolve(),
                analyticsInteractor = sdk().resolve(),
            )
        }

        registerSingleton {
            StripeAchBankFlowDelegate(
                stripeAchBankSelectionHandler = resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
                stripeAchMandateTimestampLoggingDelegate = resolve(),
                completeStripeAchPaymentSessionDelegate = resolve(),
                paymentResultRepository = sdk().resolve(),
            )
        }

        registerSingleton {
            ValidStripePublishableKeyRule()
        }

        registerSingleton {
            ValidStripeMandateDataRule(primerSettings = sdk().resolve())
        }

        registerSingleton {
            GetStripeMandateDelegate(resources = sdk().resolve<Context>().resources, primerSettings = sdk().resolve())
        }

        registerSingleton {
            StripeInitValidationRulesResolver(
                validStripePublishableKeyRule = resolve(),
                validStripeMandateDataRule = resolve(),
            )
        }
    }
}
