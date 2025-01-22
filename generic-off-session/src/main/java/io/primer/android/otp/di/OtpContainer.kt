package io.primer.android.otp.di

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.otp.PrimerOtpData
import io.primer.android.otp.implementation.configuration.data.repository.OtpConfigurationDataRepository
import io.primer.android.otp.implementation.configuration.domain.DefaultOtpConfigurationInteractor
import io.primer.android.otp.implementation.configuration.domain.OtpConfigurationInteractor
import io.primer.android.otp.implementation.configuration.domain.model.OtpConfig
import io.primer.android.otp.implementation.configuration.domain.model.OtpConfigParams
import io.primer.android.otp.implementation.payment.resume.clientToken.data.OtpClientTokenParser
import io.primer.android.otp.implementation.payment.resume.handler.OtpResumeHandler
import io.primer.android.otp.implementation.tokenization.data.datasource.OtpRemoteTokenizationDataSource
import io.primer.android.otp.implementation.tokenization.data.mapper.OtpTokenizationParamsMapper
import io.primer.android.otp.implementation.tokenization.data.model.OtpPaymentInstrumentDataRequest
import io.primer.android.otp.implementation.tokenization.data.repository.OtpTokenizationDataRepository
import io.primer.android.otp.implementation.tokenization.domain.DefaultOtpTokenizationInteractor
import io.primer.android.otp.implementation.tokenization.domain.OtpTokenizationInteractor
import io.primer.android.otp.implementation.validation.validator.OtpValidator
import io.primer.android.paymentmethods.CollectableDataValidator
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource

internal class OtpContainer(
    private val sdk: () -> SdkContainer,
    private val paymentMethodType: String,
) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory(name = paymentMethodType) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                    PrimerPaymentMethodManagerCategory.RAW_DATA.name,
                analyticsInteractor = sdk().resolve(),
            )
        }

        registerFactory<PaymentMethodConfigurationRepository<OtpConfig, OtpConfigParams>>(
            name = paymentMethodType,
        ) {
            OtpConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve(),
            )
        }

        registerFactory<OtpConfigurationInteractor>(name = paymentMethodType) {
            DefaultOtpConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory<BaseRemoteTokenizationDataSource<OtpPaymentInstrumentDataRequest>>(
            name = paymentMethodType,
        ) {
            OtpRemoteTokenizationDataSource(
                primerHttpClient = sdk().resolve(),
                apiVersion = sdk().resolve<BaseDataProvider<PrimerApiVersion>>()::provide,
            )
        }

        registerFactory<CollectableDataValidator<PrimerOtpData>> {
            OtpValidator()
        }

        registerFactory { OtpTokenizationParamsMapper() }

        registerFactory<OtpTokenizationInteractor>(name = paymentMethodType) {
            DefaultOtpTokenizationInteractor(
                tokenizationRepository =
                    OtpTokenizationDataRepository(
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
            OtpClientTokenParser()
        }

        registerFactory {
            OtpResumeHandler(
                clientTokenParser = resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
            )
        }
    }
}
