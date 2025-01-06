package io.primer.android.bancontact.di

import io.primer.android.bancontact.PrimerBancontactCardData
import io.primer.android.bancontact.implementation.configuration.data.repository.AdyenBancontactConfigurationDataRepository
import io.primer.android.bancontact.implementation.configuration.domain.AdyenBancontactConfigurationInteractor
import io.primer.android.bancontact.implementation.configuration.domain.DefaultAydenBancontactConfigurationInteractor
import io.primer.android.bancontact.implementation.configuration.domain.model.AdyenBancontactConfig
import io.primer.android.bancontact.implementation.configuration.domain.model.AdyenBancontactConfigParams
import io.primer.android.bancontact.implementation.metadata.domain.BancontactCardDataMetadataRetriever
import io.primer.android.bancontact.implementation.payment.delegate.AdyenBancontactPaymentDelegate
import io.primer.android.bancontact.implementation.payment.resume.clientToken.data.AdyenBancontactPaymentMethodClientTokenParser
import io.primer.android.bancontact.implementation.payment.resume.handler.AydenBancontactResumeHandler
import io.primer.android.bancontact.implementation.tokenization.data.datasource.AdyenBancontactRemoteTokenizationDataSource
import io.primer.android.bancontact.implementation.tokenization.data.mapper.AdyenBancontactTokenizationParamsMapper
import io.primer.android.bancontact.implementation.tokenization.data.model.AdyenBancontactPaymentInstrumentDataRequest
import io.primer.android.bancontact.implementation.tokenization.data.repository.AdyenBancontactCardTokenizationDataRepository
import io.primer.android.bancontact.implementation.tokenization.domain.AdyenBancontactTokenizationInteractor
import io.primer.android.bancontact.implementation.tokenization.domain.DefaultAdyenBancontactTokenizationInteractor
import io.primer.android.bancontact.implementation.tokenization.presentation.AdyenBancontactTokenizationDelegate
import io.primer.android.bancontact.implementation.validation.domain.AdyenBancontactInputDataValidator
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.common.utils.Constants
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.webRedirectShared.implementation.deeplink.data.repository.RedirectDeeplinkDataRepository
import io.primer.android.webRedirectShared.implementation.deeplink.domain.DefaultRedirectDeeplinkInteractor
import io.primer.android.webRedirectShared.implementation.deeplink.domain.RedirectDeeplinkInteractor
import io.primer.android.webRedirectShared.implementation.deeplink.domain.repository.RedirectDeeplinkRepository

internal class AdyenBancontactContainer(
    private val sdk: () -> SdkContainer,
    private val paymentMethodType: String,
) :
    DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory<AdyenBancontactConfigurationInteractor>(name = paymentMethodType) {
            DefaultAydenBancontactConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory<PaymentMethodConfigurationRepository<AdyenBancontactConfig, AdyenBancontactConfigParams>>(
            name = paymentMethodType,
        ) {
            AdyenBancontactConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve(),
            )
        }

        registerFactory<PaymentInputDataValidator<PrimerBancontactCardData>>(name = paymentMethodType) {
            AdyenBancontactInputDataValidator(sdk().resolve())
        }

        registerFactory(name = paymentMethodType) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                    PrimerPaymentMethodManagerCategory.RAW_DATA.name,
                analyticsInteractor = sdk().resolve(),
            )
        }

        registerFactory {
            BancontactCardDataMetadataRetriever()
        }

        registerFactory<BaseRemoteTokenizationDataSource<AdyenBancontactPaymentInstrumentDataRequest>>(
            name = paymentMethodType,
        ) {
            AdyenBancontactRemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory<RedirectDeeplinkRepository> {
            RedirectDeeplinkDataRepository(
                applicationIdProvider = sdk().resolve(Constants.APPLICATION_ID_PROVIDER_DI_KEY),
            )
        }

        registerFactory<RedirectDeeplinkInteractor>(name = paymentMethodType) {
            DefaultRedirectDeeplinkInteractor(deeplinkRepository = resolve())
        }

        registerFactory { AdyenBancontactTokenizationParamsMapper() }

        registerFactory<AdyenBancontactTokenizationInteractor>(name = paymentMethodType) {
            DefaultAdyenBancontactTokenizationInteractor(
                tokenizationRepository =
                    AdyenBancontactCardTokenizationDataRepository(
                        remoteTokenizationDataSource = sdk().resolve(paymentMethodType),
                        configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                        tokenizationParamsMapper = resolve(),
                    ),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve(),
            )
        }

        registerFactory {
            AdyenBancontactTokenizationDelegate(
                tokenizationInteractor = resolve(name = paymentMethodType),
                deeplinkInteractor = resolve<RedirectDeeplinkInteractor>(name = paymentMethodType),
                configurationInteractor = resolve(name = paymentMethodType),
            )
        }

        registerFactory {
            AdyenBancontactPaymentMethodClientTokenParser()
        }

        registerFactory<PaymentMethodPaymentDelegate>(name = paymentMethodType) {
            AdyenBancontactPaymentDelegate(
                paymentMethodTokenHandler = sdk().resolve(),
                resumePaymentHandler = sdk().resolve(),
                successHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
                resumeHandler = resolve(),
            )
        }

        registerFactory {
            AydenBancontactResumeHandler(
                clientTokenParser = resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                configurationRepository = sdk().resolve(),
                deeplinkRepository = sdk().resolve(),
            )
        }
    }
}
