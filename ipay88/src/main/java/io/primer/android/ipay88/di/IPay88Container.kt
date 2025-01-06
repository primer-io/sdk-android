package io.primer.android.ipay88.di

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.ipay88.implementation.configuration.data.repository.IPay88ConfigurationDataRepository
import io.primer.android.ipay88.implementation.configuration.domain.DefaultIPay88ConfigurationInteractor
import io.primer.android.ipay88.implementation.configuration.domain.IPay88ConfigurationInteractor
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88Config
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88ConfigParams
import io.primer.android.ipay88.implementation.deeplink.data.repository.IPay88DeeplinkDataRepository
import io.primer.android.ipay88.implementation.deeplink.domain.repository.IPay88DeeplinkRepository
import io.primer.android.ipay88.implementation.payment.resume.clientToken.data.IPay88ClientTokenParser
import io.primer.android.ipay88.implementation.payment.resume.handler.IPay88ResumeHandler
import io.primer.android.ipay88.implementation.tokenization.data.datasource.IPay88RemoteTokenizationDataSource
import io.primer.android.ipay88.implementation.tokenization.data.mapper.IPay88TokenizationParamsMapper
import io.primer.android.ipay88.implementation.tokenization.data.model.IPay88PaymentInstrumentDataRequest
import io.primer.android.ipay88.implementation.tokenization.data.repository.IPay88TokenizationDataRepository
import io.primer.android.ipay88.implementation.tokenization.domain.DefaultIPay88TokenizationInteractor
import io.primer.android.ipay88.implementation.tokenization.domain.IPay88TokenizationInteractor
import io.primer.android.ipay88.implementation.tokenization.domain.model.IPay88PaymentInstrumentParams
import io.primer.android.ipay88.implementation.validation.resolvers.IPay88ValidationRulesResolver
import io.primer.android.ipay88.implementation.validation.rules.ValidClientSessionAmountRule
import io.primer.android.ipay88.implementation.validation.rules.ValidClientSessionCountryCodeRule
import io.primer.android.ipay88.implementation.validation.rules.ValidClientSessionCurrencyRule
import io.primer.android.ipay88.implementation.validation.rules.ValidCustomerEmailRule
import io.primer.android.ipay88.implementation.validation.rules.ValidCustomerFirstNameRule
import io.primer.android.ipay88.implementation.validation.rules.ValidCustomerLastNameRule
import io.primer.android.ipay88.implementation.validation.rules.ValidProductDescriptionRule
import io.primer.android.ipay88.implementation.validation.rules.ValidRemarkRule
import io.primer.android.paymentmethods.common.utils.Constants
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository

internal class IPay88Container(private val sdk: () -> SdkContainer, private val paymentMethodType: String) :
    DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory { ValidClientSessionAmountRule() }

        registerFactory { ValidClientSessionCurrencyRule() }

        registerFactory { ValidClientSessionCountryCodeRule() }

        registerFactory { ValidProductDescriptionRule() }

        registerFactory { ValidCustomerFirstNameRule() }

        registerFactory { ValidCustomerLastNameRule() }

        registerFactory { ValidCustomerEmailRule() }

        registerFactory { ValidRemarkRule() }

        registerFactory {
            IPay88ValidationRulesResolver(
                clientSessionAmountRule = resolve(),
                clientSessionCurrencyRule = resolve(),
                clientSessionCountryCodeRule = resolve(),
                productDescriptionRule = resolve(),
                customerFirstNameRule = resolve(),
                customerLastNameRule = resolve(),
                customerEmailRule = resolve(),
                validRemarkRule = resolve(),
            )
        }

        registerSingleton<IPay88DeeplinkRepository> {
            IPay88DeeplinkDataRepository(
                sdk().resolve(dependencyName = Constants.APPLICATION_ID_PROVIDER_DI_KEY),
            )
        }

        registerFactory<PaymentMethodConfigurationRepository<IPay88Config, IPay88ConfigParams>>(
            name = paymentMethodType,
        ) {
            IPay88ConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve(),
            )
        }

        registerFactory<IPay88ConfigurationInteractor>(name = paymentMethodType) {
            DefaultIPay88ConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory { IPay88TokenizationParamsMapper() }

        registerFactory<BaseRemoteTokenizationDataSource<IPay88PaymentInstrumentDataRequest>>(
            name = paymentMethodType,
        ) {
            IPay88RemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory<TokenizationRepository<IPay88PaymentInstrumentParams>>(name = paymentMethodType) {
            IPay88TokenizationDataRepository(
                remoteTokenizationDataSource = resolve(name = paymentMethodType),
                cacheDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                tokenizationParamsMapper = resolve(),
            )
        }

        registerFactory { IPay88ClientTokenParser() }

        registerFactory {
            IPay88ResumeHandler(
                iPay88DeeplinkRepository = resolve(),
                iPay88ValidationRulesResolver = resolve(),
                clientTokenParser = resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                configurationRepository = sdk().resolve(),
                validateClientTokenRepository = sdk().resolve(),
                formattedAmountProvider = sdk().resolve(FORMATTED_AMOUNT_PROVIDER_DI_KEY),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
            )
        }

        registerFactory<IPay88TokenizationInteractor>(name = paymentMethodType) {
            DefaultIPay88TokenizationInteractor(
                tokenizationRepository = resolve(name = paymentMethodType),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve(),
            )
        }
    }

    companion object {
        const val FORMATTED_AMOUNT_PROVIDER_DI_KEY = "FORMATTED_AMOUNT_PROVIDER"
    }
}
