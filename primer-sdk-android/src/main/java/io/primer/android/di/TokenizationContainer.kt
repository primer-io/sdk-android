package io.primer.android.di

import io.primer.android.data.tokenization.datasource.RemoteTokenizationDataSource
import io.primer.android.data.tokenization.repository.TokenizationDataRepository
import io.primer.android.domain.tokenization.TokenizationInteractor
import io.primer.android.domain.tokenization.helpers.PostTokenizationEventResolver
import io.primer.android.domain.tokenization.helpers.PreTokenizationEventsResolver
import io.primer.android.domain.tokenization.repository.TokenizationRepository

internal class TokenizationContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory {
            PreTokenizationEventsResolver(
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerFactory {
            PostTokenizationEventResolver(
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { RemoteTokenizationDataSource(sdk.resolve()) }

        registerSingleton<TokenizationRepository> {
            TokenizationDataRepository(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            TokenizationInteractor(
                resolve(),
                sdk.resolve(),
                resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }
    }
}
