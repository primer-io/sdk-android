package io.primer.android.qrcode.di

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.qrcode.implementation.configuration.data.repository.QrCodeConfigurationDataRepository
import io.primer.android.qrcode.implementation.configuration.domain.DefaultQrCodeConfigurationInteractor
import io.primer.android.qrcode.implementation.configuration.domain.QrCodeConfigurationInteractor
import io.primer.android.qrcode.implementation.configuration.domain.model.QrCodeConfig
import io.primer.android.qrcode.implementation.configuration.domain.model.QrCodeConfigParams
import io.primer.android.qrcode.implementation.payment.resume.clientToken.data.QrCodeClientTokenParser
import io.primer.android.qrcode.implementation.payment.resume.handler.QrCodeResumeHandler
import io.primer.android.qrcode.implementation.tokenization.data.datasource.QrCodeRemoteTokenizationDataSource
import io.primer.android.qrcode.implementation.tokenization.data.mapper.QrCodeTokenizationParamsMapper
import io.primer.android.qrcode.implementation.tokenization.data.model.QrCodePaymentInstrumentDataRequest
import io.primer.android.qrcode.implementation.tokenization.data.repository.QrCodeTokenizationDataRepository
import io.primer.android.qrcode.implementation.tokenization.domain.DefaultQrCodeTokenizationInteractor
import io.primer.android.qrcode.implementation.tokenization.domain.QrCodeTokenizationInteractor
import io.primer.android.qrcode.implementation.tokenization.presentation.QrCodeTokenizationDelegate

internal class QrCodeContainer(
    private val sdk: () -> SdkContainer,
    private val paymentMethodType: String,
) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory(name = paymentMethodType) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                PrimerPaymentMethodManagerCategory.NATIVE_UI.name,
                analyticsInteractor = sdk().resolve(),
            )
        }

        registerFactory<PaymentMethodConfigurationRepository<QrCodeConfig, QrCodeConfigParams>>(
            name = paymentMethodType,
        ) {
            QrCodeConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve(),
            )
        }

        registerFactory<QrCodeConfigurationInteractor>(name = paymentMethodType) {
            DefaultQrCodeConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory<BaseRemoteTokenizationDataSource<QrCodePaymentInstrumentDataRequest>>(
            name = paymentMethodType,
        ) {
            QrCodeRemoteTokenizationDataSource(
                primerHttpClient = sdk().resolve(),
                apiVersion = sdk().resolve<BaseDataProvider<PrimerApiVersion>>()::provide,
            )
        }

        registerFactory { QrCodeTokenizationParamsMapper() }

        registerFactory<QrCodeTokenizationInteractor>(name = paymentMethodType) {
            DefaultQrCodeTokenizationInteractor(
                tokenizationRepository =
                QrCodeTokenizationDataRepository(
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
            QrCodeTokenizationDelegate(
                configurationInteractor = resolve(name = paymentMethodType),
                tokenizationInteractor = resolve(name = paymentMethodType),
            )
        }

        registerFactory {
            QrCodeClientTokenParser()
        }

        registerFactory {
            QrCodeResumeHandler(
                clientTokenParser = resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
            )
        }
    }
}
