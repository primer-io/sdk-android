package io.primer.checkout.orchestrator.di

import io.primer.android.analytics.di.AnalyticsContainer
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.common.utils.Constants
import io.primer.checkout.orchestrator.data.DefaultCheckoutOrchestrator
import io.primer.checkout.orchestrator.data.DefaultSdkContextProvider
import io.primer.checkout.orchestrator.data.datasource.ManifestRemoteDataSource
import io.primer.checkout.orchestrator.data.verification.ManifestSignatureVerifier
import io.primer.checkout.orchestrator.domain.CheckoutDecisionResolver
import io.primer.checkout.orchestrator.domain.CheckoutOrchestrator
import io.primer.checkout.orchestrator.domain.DefaultReturnUriProvider
import io.primer.checkout.orchestrator.domain.PaymentFlowInteractor
import io.primer.checkout.orchestrator.domain.ReturnUriProvider
import io.primer.checkout.orchestrator.domain.SdkContextProvider
import io.primer.checkout.orchestrator.domain.ui.StepUiHandlerRegistry
import io.primer.checkout.orchestrator.domain.ui.UrlOpenStepUiHandlerFactory
import okhttp3.OkHttpClient

class CheckoutOrchestratorContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton {
            ManifestSignatureVerifier(
                publicKeysB64 = MANIFEST_SIGNING_PUBLIC_KEYS,
            )
        }

        registerSingleton {
            ManifestRemoteDataSource(
                okHttpClient = sdk().resolve<OkHttpClient>(),
                signatureVerifier = resolve(),
            )
        }

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
            )
        }

        registerSingleton {
            StepUiHandlerRegistry().apply {
                register(UrlOpenStepUiHandlerFactory())
            }
        }

        registerFactory<CheckoutOrchestrator> {
            DefaultCheckoutOrchestrator(
                jsExecutor = sdk().resolve(),
                stepExecutorRegistry = sdk().resolve(),
                manifestRemoteDataSource = resolve(),
                configurationDataSource = sdk().resolve(
                    ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY,
                ),
                sdkContextProvider = resolve(),
                analyticsInteractor = sdk().resolve(),
                trustedPublicKeysB64 = MANIFEST_SIGNING_PUBLIC_KEYS,
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

    private companion object {
        @Suppress("MaximumLineLength", "MaxLineLength")
        val MANIFEST_SIGNING_PUBLIC_KEYS = listOf(
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEUM5exrePPsIdkWFL6IjKdYmEDoEHBZkoBvrApQpmDEhQ7IisLTCiP0byqN+5B5V60QjAj4I/Bw292h8gPGZyOg==", // SP_KEY_1
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERA0j68aAYtwMDagEx2FY+CBbm2+MAYviARSMxWHt1Qt8wGyVvLJ2FqIvg4m2pKfb7GqUwzuJRD/gaOrO2ZJulQ==", // SP_KEY_2
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEp7v1KpNTWcI9yJSoZYGvRxsPtciT99P2YDpISVLyD6BDD8xqJ11A8v2/elOEPaSxx5hConszht1cOlPp9YdTsA==", // SP_KEY_DEV
        )
    }
}
