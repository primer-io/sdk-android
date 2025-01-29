package io.primer.android.clientToken.di

import io.primer.android.clientToken.core.errors.data.mapper.ClientTokenErrorMapper
import io.primer.android.clientToken.core.token.data.datasource.CacheClientTokenDataSource
import io.primer.android.clientToken.core.token.data.datasource.LocalClientTokenDataSource
import io.primer.android.clientToken.core.token.data.repository.ClientTokenDataRepository
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.data.datasource.ValidationTokenDataSource
import io.primer.android.clientToken.core.validation.data.repository.ValidateClientTokenDataRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.errors.domain.ErrorMapperRegistry

class ClientTokenCoreContainer(
    private val sdk: () -> SdkContainer,
) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton<CacheClientTokenDataSource>(name = CACHE_CLIENT_TOKEN_DATA_SOURCE_DI_KEY) {
            LocalClientTokenDataSource()
        }

        registerSingleton {
            ValidationTokenDataSource(
                primerHttpClient = sdk().resolve(),
                apiVersion = sdk().resolve<BaseDataProvider<PrimerApiVersion>>()::provide,
            )
        }

        registerSingleton<ValidateClientTokenRepository> {
            ValidateClientTokenDataRepository(
                configurationDataSource = sdk().resolve(ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY),
                validateDataSource = resolve(),
            )
        }

        registerSingleton<ClientTokenRepository> {
            ClientTokenDataRepository(
                clientTokenDataSource =
                resolve<CacheClientTokenDataSource>(
                    CACHE_CLIENT_TOKEN_DATA_SOURCE_DI_KEY,
                ),
            )
        }

        sdk().resolve<ErrorMapperRegistry>().register(ClientTokenErrorMapper())
    }

    companion object {
        const val CACHE_CLIENT_TOKEN_DATA_SOURCE_DI_KEY = "CACHE_CLIENT_TOKEN_DATA_SOURCE"
    }
}
