package io.primer.android.configuration.di

import io.primer.android.analytics.di.AnalyticsContainer.Companion.TIMER_PROPERTIES_PROVIDER_DI_KEY
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.assets.ui.registry.DefaultBrandRegistry
import io.primer.android.checkoutModules.data.repository.CheckoutModuleDataRepository
import io.primer.android.checkoutModules.domain.repository.CheckoutModuleRepository
import io.primer.android.configuration.data.datasource.GlobalCacheConfigurationCacheDataSource
import io.primer.android.configuration.data.datasource.GlobalConfigurationCacheDataSource
import io.primer.android.configuration.data.datasource.LocalConfigurationDataSource
import io.primer.android.configuration.data.datasource.RemoteConfigurationDataSource
import io.primer.android.configuration.data.datasource.RemoteConfigurationResourcesDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.repository.ConfigurationDataRepository
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.DefaultConfigurationInteractor
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.infrastructure.FileProvider
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.displayMetadata.infrastructure.files.ImagesFileProvider

class ConfigurationCoreContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton<BaseCacheDataSource<ConfigurationData, ConfigurationData>>(
            CACHED_CONFIGURATION_DI_KEY,
        ) {
            LocalConfigurationDataSource()
        }

        registerSingleton<GlobalCacheConfigurationCacheDataSource>(name = GLOBAL_CACHED_CONFIGURATION_DI_KEY) {
            GlobalConfigurationCacheDataSource
        }

        registerSingleton {
            RemoteConfigurationDataSource(httpClient = sdk().resolve())
        }

        registerSingleton<FileProvider> {
            ImagesFileProvider(context = sdk().resolve())
        }

        registerSingleton<BrandRegistry> {
            DefaultBrandRegistry()
        }

        registerSingleton {
            RemoteConfigurationResourcesDataSource(
                okHttpClient = sdk().resolve(IMAGE_LOADING_CLIENT_NAME),
                imagesFileProvider = sdk().resolve(),
                timerEventProvider = sdk().resolve(TIMER_PROPERTIES_PROVIDER_DI_KEY),
            )
        }

        registerSingleton<ConfigurationRepository> {
            ConfigurationDataRepository(
                remoteConfigurationDataSource = resolve(),
                remoteConfigurationResourcesDataSource = sdk().resolve(),
                localConfigurationDataSource = resolve(CACHED_CONFIGURATION_DI_KEY),
                configurationUrlProvider = sdk().resolve(CONFIGURATION_URL_PROVIDER_DI_KEY),
                clientTokenProvider = sdk().resolve(CLIENT_TOKEN_PROVIDER_DI_KEY),
                globalConfigurationCache = resolve(GLOBAL_CACHED_CONFIGURATION_DI_KEY),
                timerEventProvider = sdk().resolve(TIMER_PROPERTIES_PROVIDER_DI_KEY),
            )
        }

        registerSingleton<ConfigurationInteractor>(name = CONFIGURATION_INTERACTOR_DI_KEY) {
            DefaultConfigurationInteractor(
                configurationRepository = resolve(),
                logReporter = sdk.invoke().resolve(),
            )
        }

        registerSingleton<CheckoutModuleRepository> {
            CheckoutModuleDataRepository(configurationDataSource = sdk.invoke().resolve(CACHED_CONFIGURATION_DI_KEY))
        }
    }

    companion object {
        const val CACHED_CONFIGURATION_DI_KEY = "CACHED_CONFIGURATION"
        const val GLOBAL_CACHED_CONFIGURATION_DI_KEY = "GLOBAL_CACHED_CONFIGURATION"
        const val CONFIGURATION_URL_PROVIDER_DI_KEY = "CONFIGURATION_URL_PROVIDER"
        const val CLIENT_TOKEN_PROVIDER_DI_KEY = "CLIENT_TOKEN_PROVIDER"
        const val CONFIGURATION_INTERACTOR_DI_KEY = "CONFIGURATION_INTERACTOR"
        const val IMAGE_LOADING_CLIENT_NAME = "IMAGE_LOADING_CLIENT"
    }
}
