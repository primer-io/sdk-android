package io.primer.android.phoneNumber.di

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.CollectableDataValidator
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.phoneNumber.PrimerPhoneNumberData
import io.primer.android.phoneNumber.implementation.configuration.data.repository.PhoneNumberConfigurationDataRepository
import io.primer.android.phoneNumber.implementation.configuration.domain.DefaultPhoneNumberConfigurationInteractor
import io.primer.android.phoneNumber.implementation.configuration.domain.PhoneNumberConfigurationInteractor
import io.primer.android.phoneNumber.implementation.configuration.domain.model.PhoneNumberConfig
import io.primer.android.phoneNumber.implementation.configuration.domain.model.PhoneNumberConfigParams
import io.primer.android.phoneNumber.implementation.payment.resume.clientToken.data.PhoneNumberClientTokenParser
import io.primer.android.phoneNumber.implementation.payment.resume.handler.PhoneNumberResumeHandler
import io.primer.android.phoneNumber.implementation.tokenization.data.datasource.PhoneNumberRemoteTokenizationDataSource
import io.primer.android.phoneNumber.implementation.tokenization.data.mapper.PhoneNumberTokenizationParamsMapper
import io.primer.android.phoneNumber.implementation.tokenization.data.model.PhoneNumberPaymentInstrumentDataRequest
import io.primer.android.phoneNumber.implementation.tokenization.data.repository.PhoneNumberTokenizationDataRepository
import io.primer.android.phoneNumber.implementation.tokenization.domain.DefaultPhoneNumberTokenizationInteractor
import io.primer.android.phoneNumber.implementation.tokenization.domain.PhoneNumberTokenizationInteractor
import io.primer.android.phoneNumber.implementation.validation.validator.PhoneNumberValidator

internal class PhoneNumberContainer(private val sdk: () -> SdkContainer, private val paymentMethodType: String) :
    DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory(name = paymentMethodType) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                PrimerPaymentMethodManagerCategory.RAW_DATA.name,
                analyticsInteractor = sdk().resolve()
            )
        }

        registerFactory<PaymentMethodConfigurationRepository<PhoneNumberConfig, PhoneNumberConfigParams>>(
            name = paymentMethodType
        ) {
            PhoneNumberConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve()
            )
        }

        registerFactory<PhoneNumberConfigurationInteractor>(name = paymentMethodType) {
            DefaultPhoneNumberConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory<BaseRemoteTokenizationDataSource<PhoneNumberPaymentInstrumentDataRequest>>(
            name = paymentMethodType
        ) {
            PhoneNumberRemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory<CollectableDataValidator<PrimerPhoneNumberData>> {
            PhoneNumberValidator(phoneMetadataRepository = sdk().resolve())
        }

        registerFactory { PhoneNumberTokenizationParamsMapper() }

        registerFactory<PhoneNumberTokenizationInteractor>(name = paymentMethodType) {
            DefaultPhoneNumberTokenizationInteractor(
                tokenizationRepository = PhoneNumberTokenizationDataRepository(
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
            PhoneNumberClientTokenParser()
        }

        registerFactory {
            PhoneNumberResumeHandler(
                clientTokenParser = resolve(),
                validateClientTokenRepository = sdk().resolve(),
                clientTokenRepository = sdk().resolve(),
                checkoutAdditionalInfoHandler = sdk().resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve()
            )
        }
    }
}
