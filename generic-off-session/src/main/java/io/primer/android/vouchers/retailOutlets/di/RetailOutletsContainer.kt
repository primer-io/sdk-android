package io.primer.android.vouchers.retailOutlets.di

import io.primer.android.PrimerRetailerData
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.paymentmethods.PaymentInputDataValidator
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.vouchers.retailOutlets.implementation.configuration.data.repository.RetailOutletsConfigurationDataRepository
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.DefaultRetailOutletsConfigurationInteractor
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.RetailOutletsConfigurationInteractor
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model.RetailOutletsConfig
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model.RetailOutletsConfigParams
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.data.RetailOutletsClientTokenParser
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.handler.RetailOutletsResumeHandler
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.datasource.LocalRetailOutletDataSource
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.datasource.RemoteRetailOutletDataSource
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.repository.RetailOutletDataRepository
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.RetailOutletInteractor
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.datasource.RetailOutletsRemoteTokenizationDataSource
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.mapper.RetailOutletsTokenizationParamsMapper
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model.RetailOutletsPaymentInstrumentDataRequest
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.repository.RetailerOutletsTokenizationDataRepository
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain.DefaultRetailOutletsTokenizationInteractor
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.domain.RetailOutletsTokenizationInteractor
import io.primer.android.vouchers.retailOutlets.implementation.tokenization.presentation.RetailOutletsTokenizationDelegate
import io.primer.android.vouchers.retailOutlets.implementation.validation.domain.RetailerOutletInputValidator

internal class RetailOutletsContainer(
    private val sdk: () -> SdkContainer,
    private val paymentMethodType: String,
) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton { LocalRetailOutletDataSource() }

        registerSingleton {
            RemoteRetailOutletDataSource(
                primerHttpClient = sdk().resolve(),
            )
        }

        registerSingleton<RetailOutletRepository>(name = paymentMethodType) {
            RetailOutletDataRepository(
                remoteRetailOutletBankDataSource = resolve(),
                localRetailOutletDataSource = sdk().resolve(),
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
            )
        }

        registerSingleton(name = paymentMethodType) {
            RetailOutletInteractor(
                configurationRepository = sdk().resolve(),
                retailOutletRepository = resolve(name = paymentMethodType),
            )
        }

        registerFactory<PaymentInputDataValidator<PrimerRetailerData>>(name = paymentMethodType) {
            RetailerOutletInputValidator(retailerOutletRepository = resolve(name = paymentMethodType))
        }

        registerFactory(name = paymentMethodType) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                    PrimerPaymentMethodManagerCategory.RAW_DATA.name,
                analyticsInteractor = sdk().resolve(),
            )
        }

        registerFactory<PaymentMethodConfigurationRepository<RetailOutletsConfig, RetailOutletsConfigParams>>(
            name = paymentMethodType,
        ) {
            RetailOutletsConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve(),
            )
        }

        registerFactory<RetailOutletsConfigurationInteractor>(name = paymentMethodType) {
            DefaultRetailOutletsConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory<BaseRemoteTokenizationDataSource<RetailOutletsPaymentInstrumentDataRequest>>(
            name = paymentMethodType,
        ) {
            RetailOutletsRemoteTokenizationDataSource(
                primerHttpClient = sdk().resolve(),
                apiVersion = sdk().resolve<BaseDataProvider<PrimerApiVersion>>()::provide,
            )
        }

        registerFactory { RetailOutletsTokenizationParamsMapper() }

        registerFactory<RetailOutletsTokenizationInteractor>(name = paymentMethodType) {
            DefaultRetailOutletsTokenizationInteractor(
                tokenizationRepository =
                    RetailerOutletsTokenizationDataRepository(
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
            RetailOutletsTokenizationDelegate(
                configurationInteractor = resolve(name = paymentMethodType),
                tokenizationInteractor = resolve(name = paymentMethodType),
            )
        }

        registerFactory {
            RetailOutletsClientTokenParser()
        }

        registerFactory {
            RetailOutletsResumeHandler(
                clientTokenParser = resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                retailOutletRepository = resolve(name = paymentMethodType),
                tokenizedPaymentMethodRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
            )
        }
    }
}
