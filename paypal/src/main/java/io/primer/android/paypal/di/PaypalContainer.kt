package io.primer.android.paypal.di

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.logging.WhitelistedHttpBodyKeyProviderRegistry
import io.primer.android.paymentmethods.common.utils.Constants
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.domain.repository.TokenizationRepository
import io.primer.android.paypal.implementation.configuration.data.repository.PaypalConfigurationDataRepository
import io.primer.android.paypal.implementation.configuration.domain.DefaultPaypalConfigurationInteractor
import io.primer.android.paypal.implementation.configuration.domain.PaypalConfigurationInteractor
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfig
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfigParams
import io.primer.android.paypal.implementation.tokenization.data.datasource.PaypalRemoteTokenizationDataSource
import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalConfirmBillingAgreementDataSource
import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalCreateBillingAgreementDataSource
import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalCreateOrderDataSource
import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalOrderInfoDataSource
import io.primer.android.paypal.implementation.tokenization.data.mapper.PaypalTokenizationParamsMapper
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalConfirmBillingAgreementDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateBillingAgreementDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateOrderDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalOrderInfoDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalPaymentInstrumentDataRequest
import io.primer.android.paypal.implementation.tokenization.data.repository.PaypalConfirmBillingAgreementDataRepository
import io.primer.android.paypal.implementation.tokenization.data.repository.PaypalCreateBillingAgreementDataRepository
import io.primer.android.paypal.implementation.tokenization.data.repository.PaypalCreateOrderDataRepository
import io.primer.android.paypal.implementation.tokenization.data.repository.PaypalOrderInfoDataRepository
import io.primer.android.paypal.implementation.tokenization.data.repository.PaypalTokenizationDataRepository
import io.primer.android.paypal.implementation.tokenization.domain.DefaultPaypalTokenizationInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalConfirmBillingAgreementInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalCreateBillingAgreementInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalCreateOrderInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalOrderInfoInteractor
import io.primer.android.paypal.implementation.tokenization.domain.PaypalTokenizationInteractor
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalPaymentInstrumentParams
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalConfirmBillingAgreementRepository
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalCreateBillingAgreementRepository
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalCreateOrderRepository
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalInfoRepository
import io.primer.android.paypal.implementation.tokenization.presentation.PaypalTokenizationCollectorDelegate
import io.primer.android.paypal.implementation.validation.resolvers.PaypalCheckoutOrderInfoValidationRulesResolver
import io.primer.android.paypal.implementation.validation.resolvers.PaypalCheckoutOrderValidationRulesResolver
import io.primer.android.paypal.implementation.validation.resolvers.PaypalValidBillingAgreementTokenRule
import io.primer.android.paypal.implementation.validation.resolvers.PaypalValidOrderAmountRule
import io.primer.android.paypal.implementation.validation.resolvers.PaypalValidOrderCurrencyRule
import io.primer.android.paypal.implementation.validation.resolvers.PaypalValidOrderTokenRule
import io.primer.android.paypal.implementation.validation.resolvers.PaypalVaultValidationRulesResolver

internal class PaypalContainer(private val sdk: () -> SdkContainer, private val paymentMethodType: String) :
    DependencyContainer() {

    override fun registerInitialDependencies() {
        sdk().resolve<WhitelistedHttpBodyKeyProviderRegistry>().apply {
            listOf(
                PaypalCreateOrderDataRequest.provider,
                PaypalOrderInfoDataRequest.provider,
                PaypalCreateBillingAgreementDataRequest.provider,
                PaypalConfirmBillingAgreementDataRequest.provider
            ).forEach(::register)
        }

        registerSingleton { RemotePaypalOrderInfoDataSource(sdk().resolve()) }

        registerSingleton<PaypalInfoRepository> {
            PaypalOrderInfoDataRepository(
                resolve(),
                sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY)
            )
        }

        registerSingleton {
            PaypalOrderInfoInteractor(
                paypalInfoRepository = resolve(),
                validationRulesResolver = resolve()
            )
        }

        registerSingleton { RemotePaypalCreateOrderDataSource(sdk().resolve()) }

        registerSingleton<PaypalCreateOrderRepository> {
            PaypalCreateOrderDataRepository(
                createOrderDataSource = resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY)
            )
        }

        registerSingleton {
            PaypalCreateOrderInteractor(
                createOrderRepository = resolve()
            )
        }

        registerSingleton { RemotePaypalCreateBillingAgreementDataSource(sdk().resolve()) }

        registerSingleton { RemotePaypalConfirmBillingAgreementDataSource(sdk().resolve()) }

        registerSingleton<PaypalCreateBillingAgreementRepository> {
            PaypalCreateBillingAgreementDataRepository(
                createBillingAgreementDataSource = resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY)
            )
        }

        registerSingleton<PaypalConfirmBillingAgreementRepository> {
            PaypalConfirmBillingAgreementDataRepository(
                confirmBillingAgreementDataSource = resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY)
            )
        }

        registerSingleton {
            PaypalCreateBillingAgreementInteractor(
                createOrderRepository = resolve()
            )
        }

        registerSingleton {
            PaypalConfirmBillingAgreementInteractor(
                confirmBillingAgreementRepository = resolve(),
                validationRulesResolver = resolve()
            )
        }

        registerSingleton { PaypalValidOrderTokenRule() }

        registerSingleton {
            PaypalCheckoutOrderInfoValidationRulesResolver(
                resolve()
            )
        }

        registerSingleton { PaypalValidBillingAgreementTokenRule() }

        registerSingleton {
            PaypalVaultValidationRulesResolver(
                resolve()
            )
        }

        registerSingleton { PaypalValidOrderAmountRule() }

        registerSingleton { PaypalValidOrderCurrencyRule() }

        registerSingleton { PaypalCheckoutOrderValidationRulesResolver(resolve(), resolve()) }

        registerFactory<PaymentMethodConfigurationRepository<PaypalConfig, PaypalConfigParams>>(
            name = paymentMethodType
        ) {
            PaypalConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                applicationIdProvider = sdk().resolve(Constants.APPLICATION_ID_PROVIDER_DI_KEY)
            )
        }

        registerFactory<PaypalConfigurationInteractor>(name = paymentMethodType) {
            DefaultPaypalConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory { PaypalTokenizationParamsMapper() }

        registerFactory<BaseRemoteTokenizationDataSource<PaypalPaymentInstrumentDataRequest>>(
            name = paymentMethodType
        ) {
            PaypalRemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory<TokenizationRepository<PaypalPaymentInstrumentParams>>(name = paymentMethodType) {
            PaypalTokenizationDataRepository(
                remoteTokenizationDataSource = resolve(name = paymentMethodType),
                cacheDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                tokenizationParamsMapper = resolve()
            )
        }

        registerFactory<PaypalTokenizationInteractor>(name = paymentMethodType) {
            DefaultPaypalTokenizationInteractor(
                tokenizationRepository = resolve(name = paymentMethodType),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                preTokenizationHandler = sdk().resolve(),
                logReporter = sdk().resolve()
            )
        }

        registerFactory(name = paymentMethodType) {
            PaypalTokenizationCollectorDelegate(
                configurationInteractor = resolve(name = paymentMethodType),
                createOrderInteractor = resolve(),
                createBillingAgreementInteractor = resolve()
            )
        }
    }
}
