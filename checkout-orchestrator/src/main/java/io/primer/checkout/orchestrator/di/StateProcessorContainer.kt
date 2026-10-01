package io.primer.checkout.orchestrator.di

import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.checkout.orchestrator.data.StateProcessorLoader
import io.primer.checkout.orchestrator.data.datasource.ManifestRemoteDataSource
import io.primer.checkout.orchestrator.data.verification.ManifestSignatureVerifier
import io.primer.checkout.orchestrator.domain.PreloadStateProcessorInteractor
import okhttp3.OkHttpClient

// Registered with the SDK, so loading can start before the configuration arrives.
class StateProcessorContainer(
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

        registerSingleton {
            StateProcessorLoader(
                manifestRemoteDataSource = resolve(),
                jsExecutor = sdk().resolve(),
            )
        }

        registerFactory {
            PreloadStateProcessorInteractor(
                stateProcessorLoader = resolve(),
                clientTokenProvider = sdk().resolve(ConfigurationCoreContainer.CLIENT_TOKEN_PROVIDER_DI_KEY),
                logReporter = sdk().resolve(),
            )
        }
    }

    internal companion object {
        @Suppress("MaximumLineLength", "MaxLineLength")
        val MANIFEST_SIGNING_PUBLIC_KEYS = listOf(
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEUM5exrePPsIdkWFL6IjKdYmEDoEHBZkoBvrApQpmDEhQ7IisLTCiP0byqN+5B5V60QjAj4I/Bw292h8gPGZyOg==", // SP_KEY_1
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERA0j68aAYtwMDagEx2FY+CBbm2+MAYviARSMxWHt1Qt8wGyVvLJ2FqIvg4m2pKfb7GqUwzuJRD/gaOrO2ZJulQ==", // SP_KEY_2
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEp7v1KpNTWcI9yJSoZYGvRxsPtciT99P2YDpISVLyD6BDD8xqJ11A8v2/elOEPaSxx5hConszht1cOlPp9YdTsA==", // SP_KEY_DEV
        )
    }
}
