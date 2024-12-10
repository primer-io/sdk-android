package io.primer.android.webredirect.di

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.common.utils.Constants
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.webRedirectShared.implementation.deeplink.data.repository.RedirectDeeplinkDataRepository
import io.primer.android.webRedirectShared.implementation.deeplink.domain.DefaultRedirectDeeplinkInteractor
import io.primer.android.webRedirectShared.implementation.deeplink.domain.RedirectDeeplinkInteractor
import io.primer.android.webRedirectShared.implementation.deeplink.domain.repository.RedirectDeeplinkRepository
import io.primer.android.webredirect.implementation.configuration.data.repository.WebRedirectConfigurationDataRepository
import io.primer.android.webredirect.implementation.configuration.domain.DefaultWebRedirectConfigurationInteractor
import io.primer.android.webredirect.implementation.configuration.domain.WebRedirectConfigurationInteractor
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfig
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfigParams
import io.primer.android.webredirect.implementation.payment.resume.clientToken.data.WebRedirectPaymentMethodClientTokenParser
import io.primer.android.webredirect.implementation.payment.resume.handler.WebRedirectResumeHandler
import io.primer.android.webredirect.implementation.tokenization.data.datasource.WebRedirectRemoteTokenizationDataSource
import io.primer.android.webredirect.implementation.tokenization.data.mapper.WebRedirectTokenizationParamsMapper
import io.primer.android.webredirect.implementation.tokenization.data.model.WebRedirectPaymentInstrumentDataRequest
import io.primer.android.webredirect.implementation.tokenization.data.repository.WebRedirectTokenizationDataRepository
import io.primer.android.webredirect.implementation.tokenization.domain.DefaultWebRedirectTokenizationInteractor
import io.primer.android.webredirect.implementation.tokenization.domain.WebRedirectTokenizationInteractor
import io.primer.android.webredirect.implementation.tokenization.domain.model.WebRedirectPaymentInstrumentParams
import io.primer.android.webredirect.implementation.tokenization.domain.platform.PlatformResolver

internal class WebRedirectContainer(private val sdk: () -> SdkContainer, private val paymentMethodType: String) :
    DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory<RedirectDeeplinkRepository> {
            RedirectDeeplinkDataRepository(
                applicationIdProvider = sdk().resolve(Constants.APPLICATION_ID_PROVIDER_DI_KEY)
            )
        }

        registerFactory<RedirectDeeplinkInteractor>(name = paymentMethodType) {
            DefaultRedirectDeeplinkInteractor(deeplinkRepository = resolve())
        }

        registerFactory<PlatformResolver>(name = paymentMethodType) { PlatformResolver() }

        registerFactory<PaymentMethodConfigurationRepository<WebRedirectConfig, WebRedirectConfigParams>>(
            name = paymentMethodType
        ) {
            WebRedirectConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve()
            )
        }

        registerFactory<WebRedirectConfigurationInteractor>(name = paymentMethodType) {
            DefaultWebRedirectConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory { WebRedirectTokenizationParamsMapper() }

        registerFactory<BaseRemoteTokenizationDataSource<WebRedirectPaymentInstrumentDataRequest>>(
            name = paymentMethodType
        ) {
            WebRedirectRemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory<TokenizationRepository<WebRedirectPaymentInstrumentParams>>(name = paymentMethodType) {
            WebRedirectTokenizationDataRepository(
                remoteTokenizationDataSource = resolve(name = paymentMethodType),
                cacheDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                tokenizationParamsMapper = resolve()
            )
        }

        registerFactory { WebRedirectPaymentMethodClientTokenParser() }

        registerFactory {
            WebRedirectResumeHandler(
                clientTokenParser = resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                configurationRepository = sdk().resolve(),
                deeplinkRepository = sdk().resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve()
            )
        }

        registerFactory<WebRedirectTokenizationInteractor>(name = paymentMethodType) {
            DefaultWebRedirectTokenizationInteractor(
                tokenizationRepository = resolve(name = paymentMethodType),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve()
            )
        }
    }
}
