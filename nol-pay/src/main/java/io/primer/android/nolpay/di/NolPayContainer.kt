package io.primer.android.nolpay.di

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.nolpay.implementation.common.data.datasource.RemoteNolPaySecretDataSource
import io.primer.android.nolpay.implementation.common.data.model.NolPaySecretDataRequest
import io.primer.android.nolpay.implementation.common.data.repository.NolPayAppSecretDataRepository
import io.primer.android.nolpay.implementation.common.data.repository.NolPaySdkInitSdkInitConfigurationDataRepository
import io.primer.android.nolpay.implementation.common.domain.NolPaySdkInitInteractor
import io.primer.android.nolpay.implementation.common.domain.repository.NolPayAppSecretRepository
import io.primer.android.nolpay.implementation.common.domain.repository.NolPayCompletePaymentRepository
import io.primer.android.nolpay.implementation.common.domain.repository.NolPaySdkInitConfigurationRepository
import io.primer.android.nolpay.implementation.configuration.data.repository.NolPayConfigurationDataRepository
import io.primer.android.nolpay.implementation.configuration.domain.NolPayConfigurationInteractor
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfig
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfigParams
import io.primer.android.nolpay.implementation.linkCard.domain.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.nolpay.implementation.linkCard.domain.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.nolpay.implementation.linkCard.domain.NolPayLinkPaymentCardInteractor
import io.primer.android.nolpay.implementation.linkCard.domain.validation.NolPayLinkDataValidatorRegistry
import io.primer.android.nolpay.implementation.linkCard.presentation.NolPayLinkPaymentCardDelegate
import io.primer.android.nolpay.implementation.listCards.domain.NolPayGetLinkedCardsInteractor
import io.primer.android.nolpay.implementation.listCards.presentation.NolPayGetLinkedCardsDelegate
import io.primer.android.nolpay.implementation.paymentCard.completion.data.datasource.RemoteNolPayCompletePaymentDataSource
import io.primer.android.nolpay.implementation.paymentCard.completion.data.repository.NolPayCompletePaymentDataRepository
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.NolPayCompletePaymentInteractor
import io.primer.android.nolpay.implementation.paymentCard.completion.domain.NolPayRequestPaymentInteractor
import io.primer.android.nolpay.implementation.paymentCard.payment.delegate.NolPayPaymentDelegate
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.data.NolPayClientTokenParser
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.handler.NolPayResumeHandler
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.datasource.NolPayRemoteTokenizationDataSource
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.mapper.NolPayTokenizationParamsMapper
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model.NolPayPaymentInstrumentDataRequest
import io.primer.android.nolpay.implementation.paymentCard.tokenization.data.repository.NolPayTokenizationDataRepository
import io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.DefaultNolPayTokenizationInteractor
import io.primer.android.nolpay.implementation.paymentCard.tokenization.domain.NolPayTokenizationInteractor
import io.primer.android.nolpay.implementation.paymentCard.tokenization.presentation.NolPayTokenizationDelegate
import io.primer.android.nolpay.implementation.unlinkCard.domain.NolPayGetUnlinkPaymentCardOTPInteractor
import io.primer.android.nolpay.implementation.unlinkCard.domain.NolPayUnlinkPaymentCardInteractor
import io.primer.android.nolpay.implementation.unlinkCard.presentation.NolPayUnlinkPaymentCardDelegate
import io.primer.android.nolpay.implementation.validation.NolPayPaymentDataValidatorRegistry
import io.primer.android.nolpay.implementation.validation.NolPayUnlinkDataValidatorRegistry
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate
import io.primer.android.paymentmethods.analytics.delegate.SdkAnalyticsValidationErrorLoggingDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.di.PaymentsContainer
import io.primer.android.phoneMetadata.domain.PhoneMetadataInteractor
import io.primer.nolpay.api.PrimerNolPay

internal class NolPayContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    @Suppress("LongMethod")
    override fun registerInitialDependencies() {
        val paymentMethodType = PaymentMethodType.NOL_PAY.name

        sdk().resolve<WhitelistedHttpBodyKeyProviderRegistry>().apply {
            listOf(
                NolPaySecretDataRequest.provider
            ).forEach(::register)
        }

        registerFactory<NolPaySdkInitConfigurationRepository> {
            NolPaySdkInitSdkInitConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY)
            )
        }

        registerFactory { PrimerNolPay }

        registerFactory { RemoteNolPaySecretDataSource(primerHttpClient = sdk().resolve()) }
        registerFactory { RemoteNolPayCompletePaymentDataSource(httpClient = sdk().resolve()) }

        registerFactory<NolPayAppSecretRepository> {
            NolPayAppSecretDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                nolPaySecretDataSource = resolve()
            )
        }

        registerFactory<NolPayCompletePaymentRepository> {
            NolPayCompletePaymentDataRepository(
                completePaymentDataSource = resolve()
            )
        }

        registerSingleton {
            NolPaySdkInitInteractor(
                secretRepository = resolve(),
                nolPaySdkInitConfigurationRepository = resolve(),
                nolPay = resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerFactory { NolPayGetLinkPaymentCardTokenInteractor(nolPay = resolve()) }
        registerFactory { NolPayGetLinkPaymentCardOTPInteractor(nolPay = resolve()) }
        registerFactory { NolPayLinkPaymentCardInteractor(nolPay = resolve()) }
        registerFactory { NolPayGetUnlinkPaymentCardOTPInteractor(nolPay = resolve()) }
        registerFactory { NolPayUnlinkPaymentCardInteractor(nolPay = resolve()) }
        registerFactory { NolPayRequestPaymentInteractor(nolPay = resolve()) }
        registerFactory { NolPayGetLinkedCardsInteractor(nolPay = resolve()) }
        registerFactory { NolPayCompletePaymentInteractor(completePaymentRepository = resolve()) }
        registerFactory { PhoneMetadataInteractor(phoneMetadataRepository = sdk().resolve()) }

        registerFactory { NolPayLinkDataValidatorRegistry() }
        registerFactory { NolPayUnlinkDataValidatorRegistry() }
        registerFactory { NolPayPaymentDataValidatorRegistry() }

        registerFactory(name = PaymentMethodType.NOL_PAY.name) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                PrimerPaymentMethodManagerCategory.NOL_PAY.name,
                analyticsInteractor = sdk().resolve()
            )
        }

        registerFactory {
            NolPayLinkPaymentCardDelegate(
                getLinkPaymentCardTokenInteractor = resolve(),
                getLinkPaymentCardOTPInteractor = resolve(),
                linkPaymentCardInteractor = resolve(),
                phoneMetadataInteractor = resolve(),
                sdkInitInteractor = resolve()
            )
        }

        registerFactory {
            NolPayUnlinkPaymentCardDelegate(
                unlinkPaymentCardOTPInteractor = resolve(),
                unlinkPaymentCardInteractor = resolve(),
                phoneMetadataInteractor = resolve(),
                sdkInitInteractor = resolve()
            )
        }

        registerFactory {
            NolPayGetLinkedCardsDelegate(
                getLinkedCardsInteractor = resolve(),
                phoneMetadataInteractor = resolve(),
                sdkInitInteractor = resolve()
            )
        }

        registerFactory<
            PaymentMethodConfigurationRepository<NolPayConfig, NolPayConfigParams>>(
            name = paymentMethodType
        ) {
            NolPayConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                primerSettings = sdk().resolve()
            )
        }

        registerFactory<NolPayConfigurationInteractor>(name = paymentMethodType) {
            PaymentMethodConfigurationInteractor(
                configurationRepository = resolve(name = paymentMethodType)
            )
        }

        registerFactory<BaseRemoteTokenizationDataSource<NolPayPaymentInstrumentDataRequest>>(
            name = paymentMethodType
        ) {
            NolPayRemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory { NolPayTokenizationParamsMapper() }

        registerFactory<NolPayTokenizationInteractor>(name = paymentMethodType) {
            DefaultNolPayTokenizationInteractor(
                tokenizationRepository = NolPayTokenizationDataRepository(
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
            NolPayTokenizationDelegate(
                configurationInteractor = resolve(
                    name = paymentMethodType
                ),
                tokenizationInteractor = resolve(
                    name = paymentMethodType
                ),
                phoneMetadataInteractor = sdk().resolve()
            )
        }

        registerFactory {
            NolPayClientTokenParser()
        }

        registerFactory {
            NolPayResumeHandler(
                clientTokenParser = resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve()
            )
        }

        registerFactory {
            NolPayPaymentDelegate(
                requestPaymentInteractor = resolve(),
                completePaymentInteractor = resolve(),
                pollingInteractor = sdk().resolve(PaymentsContainer.POLLING_INTERACTOR_DI_KEY),
                paymentMethodTokenHandler = sdk().resolve(),
                resumePaymentHandler = sdk().resolve(),
                successHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
                resumeHandler = resolve()
            )
        }

        registerFactory(name = paymentMethodType) {
            SdkAnalyticsErrorLoggingDelegate(analyticsInteractor = sdk().resolve())
        }

        registerFactory(name = paymentMethodType) {
            SdkAnalyticsValidationErrorLoggingDelegate(analyticsInteractor = sdk().resolve())
        }
    }
}
