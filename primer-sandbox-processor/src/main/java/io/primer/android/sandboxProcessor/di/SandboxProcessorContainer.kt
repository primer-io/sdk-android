package io.primer.android.sandboxProcessor.di

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.sandboxProcessor.implementation.configuration.data.repository.SandboxProcessorConfigurationDataRepository
import io.primer.android.sandboxProcessor.implementation.configuration.domain.DefaultSandboxProcessorConfigurationInteractor
import io.primer.android.sandboxProcessor.implementation.configuration.domain.ProcessorTestConfigurationInteractor
import io.primer.android.sandboxProcessor.implementation.configuration.domain.model.SandboxProcessorConfig
import io.primer.android.sandboxProcessor.implementation.configuration.domain.model.SandboxProcessorConfigParams
import io.primer.android.sandboxProcessor.implementation.payment.delegate.SandboxProcessorPaymentDelegate
import io.primer.android.sandboxProcessor.implementation.tokenization.data.datasource.SandboxProcessorRemoteTokenizationDataSource
import io.primer.android.sandboxProcessor.implementation.tokenization.data.mapper.SandboxProcessorTokenizationParamsMapper
import io.primer.android.sandboxProcessor.implementation.tokenization.data.model.SandboxProcessorPaymentInstrumentDataRequest
import io.primer.android.sandboxProcessor.implementation.tokenization.data.repository.SandboxProcessorTokenizationDataRepository
import io.primer.android.sandboxProcessor.implementation.tokenization.domain.DefaultProcessorTestTokenizationInteractor
import io.primer.android.sandboxProcessor.implementation.tokenization.domain.ProcessorTestTokenizationInteractor
import io.primer.android.sandboxProcessor.implementation.tokenization.presentation.SandboxProcessorTokenizationDelegate

internal class SandboxProcessorContainer(private val sdk: () -> SdkContainer, private val paymentMethodType: String) :
    DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory(name = paymentMethodType) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                    PrimerPaymentMethodManagerCategory.NATIVE_UI.name,
                analyticsInteractor = sdk().resolve(),
            )
        }

        registerFactory<PaymentMethodConfigurationRepository<SandboxProcessorConfig, SandboxProcessorConfigParams>>(
            name = paymentMethodType,
        ) {
            SandboxProcessorConfigurationDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                settings = sdk().resolve(),
            )
        }

        registerFactory<ProcessorTestConfigurationInteractor>(name = paymentMethodType) {
            DefaultSandboxProcessorConfigurationInteractor(configurationRepository = resolve(name = paymentMethodType))
        }

        registerFactory<BaseRemoteTokenizationDataSource<SandboxProcessorPaymentInstrumentDataRequest>>(
            name = paymentMethodType,
        ) {
            SandboxProcessorRemoteTokenizationDataSource(primerHttpClient = sdk().resolve())
        }

        registerFactory { SandboxProcessorTokenizationParamsMapper() }

        registerFactory<ProcessorTestTokenizationInteractor>(name = paymentMethodType) {
            DefaultProcessorTestTokenizationInteractor(
                tokenizationRepository =
                    SandboxProcessorTokenizationDataRepository(
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
            SandboxProcessorTokenizationDelegate(
                configurationInteractor = resolve(name = paymentMethodType),
                tokenizationInteractor = resolve(name = paymentMethodType),
            )
        }

        registerFactory {
            SandboxProcessorPaymentDelegate(
                paymentMethodTokenHandler = sdk().resolve(),
                resumePaymentHandler = sdk().resolve(),
                successHandler = sdk().resolve(),
                errorHandler = sdk().resolve(),
                baseErrorResolver = sdk().resolve(),
                tokenizedPaymentMethodRepository = sdk().resolve(),
            )
        }
    }
}
