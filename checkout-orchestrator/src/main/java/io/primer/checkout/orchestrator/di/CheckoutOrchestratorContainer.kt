package io.primer.checkout.orchestrator.di

import io.primer.android.analytics.di.AnalyticsContainer
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.common.utils.Constants
import io.primer.checkout.orchestrator.BackendDrivenPaymentMethodChecker
import io.primer.checkout.orchestrator.data.DefaultCheckoutOrchestrator
import io.primer.checkout.orchestrator.data.DefaultSdkContextProvider
import io.primer.checkout.orchestrator.data.datasource.ClientRequirementsDataSource
import io.primer.checkout.orchestrator.data.repository.ClientRequirementsDataRepository
import io.primer.checkout.orchestrator.domain.CheckoutDecisionResolver
import io.primer.checkout.orchestrator.domain.CheckoutOrchestrator
import io.primer.checkout.orchestrator.domain.DefaultReturnUriProvider
import io.primer.checkout.orchestrator.domain.PaymentFlowInteractor
import io.primer.checkout.orchestrator.domain.ReturnUriProvider
import io.primer.checkout.orchestrator.domain.SdkContextProvider
import io.primer.checkout.orchestrator.domain.repository.ClientRequirementsRepository
import io.primer.checkout.orchestrator.domain.ui.StepUiHandlerRegistry
import io.primer.checkout.orchestrator.domain.ui.UrlOpenStepUiHandlerFactory

class CheckoutOrchestratorContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory {
            CheckoutDecisionResolver(
                logReporter = sdk().resolve(),
            )
        }

        registerFactory<ReturnUriProvider> {
            DefaultReturnUriProvider(
                applicationIdProvider = sdk().resolve(
                    Constants.APPLICATION_ID_PROVIDER_DI_KEY,
                ),
            )
        }

        registerFactory<SdkContextProvider> {
            DefaultSdkContextProvider(
                context = sdk().resolve(),
                configurationDataSource = sdk().resolve(
                    ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY,
                ),
                checkoutSessionIdProvider = sdk().resolve(
                    AnalyticsContainer.CHECKOUT_SESSION_ID_PROVIDER_DI_KEY,
                ),
                applicationIdProvider = sdk().resolve(
                    Constants.APPLICATION_ID_PROVIDER_DI_KEY,
                ),
                settings = sdk().resolve(),
                analyticsDataProvider = sdk().resolve(
                    AnalyticsContainer.ANALYTICS_DATA_DI_KEY,
                ),
                returnUriProvider = resolve(),
            )
        }

        registerClientRequirementsCheck()

        registerSingleton {
            StepUiHandlerRegistry().apply {
                register(UrlOpenStepUiHandlerFactory())
            }
        }

        registerFactory<CheckoutOrchestrator> {
            DefaultCheckoutOrchestrator(
                stateProcessorLoader = sdk().resolve(),
                stepExecutorRegistry = sdk().resolve(),
                configurationDataSource = sdk().resolve(
                    ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY,
                ),
                sdkContextProvider = resolve(),
                analyticsInteractor = sdk().resolve(),
                trustedPublicKeysB64 = StateProcessorContainer.MANIFEST_SIGNING_PUBLIC_KEYS,
            )
        }

        registerFactory {
            PaymentFlowInteractor(
                repository = sdk().resolve(),
                configurationRepository = sdk().resolve(),
                orchestrator = resolve(),
            )
        }
    }

    private fun registerClientRequirementsCheck() {
        registerFactory {
            ClientRequirementsDataSource(
                stateProcessorLoader = sdk().resolve(),
            )
        }

        registerSingleton<ClientRequirementsRepository> {
            ClientRequirementsDataRepository(
                clientRequirementsDataSource = resolve(),
                configurationDataSource = sdk().resolve(
                    ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY,
                ),
                sdkContextProvider = resolve(),
                stepExecutorRegistry = sdk().resolve(),
                logReporter = sdk().resolve(),
            )
        }

        registerFactory {
            BackendDrivenPaymentMethodChecker(
                clientRequirementsRepository = resolve(),
                settings = sdk().resolve(),
                analyticsInteractor = sdk().resolve(),
                logReporter = sdk().resolve(),
            )
        }
    }
}
