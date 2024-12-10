package io.primer.android.components.di

import android.content.Context
import io.primer.android.analytics.data.models.AnalyticsData
import io.primer.android.analytics.di.AnalyticsContainer
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.clientToken.core.token.data.datasource.CacheClientTokenDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.clientToken.core.token.data.model.ClientToken
import io.primer.android.clientToken.di.ClientTokenCoreContainer
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.core.utils.BaseDataProvider

internal class SharedContainer(
    private val context: Context,
    private val config: PrimerConfig,
    private val clientToken: ClientToken,
    private val sdk: () -> SdkContainer
) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { context }

        registerSingleton { config }

        registerSingleton { config.settings }

        registerSingleton(ConfigurationCoreContainer.CONFIGURATION_URL_PROVIDER_DI_KEY) {
            BaseDataProvider { clientToken.configurationUrl.orEmpty() }
        }

        registerSingleton(ConfigurationCoreContainer.CLIENT_TOKEN_PROVIDER_DI_KEY) {
            BaseDataProvider { config.clientTokenBase64.orEmpty() }
        }

        registerSingleton(AnalyticsContainer.PCI_URL_PROVIDER_DI_KEY) {
            BaseDataProvider {
                sdk().resolve<CacheConfigurationDataSource>(
                    ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY
                ).get().pciUrl
            }
        }

        registerSingleton(AnalyticsContainer.ANALYTICS_DATA_DI_KEY) {
            BaseDataProvider {
                AnalyticsData(
                    sdkIntegrationType = config.settings.sdkIntegrationType,
                    paymentHandling = config.settings.paymentHandling.name,
                    analyticsUrl = clientToken.analyticsUrlV2,
                    clientSessionId = sdk().resolve<BaseCacheDataSource<ConfigurationData, ConfigurationData>>(
                        ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY
                    )
                        .get().clientSession.clientSessionId,
                    orderId = sdk().resolve<BaseCacheDataSource<ConfigurationData, ConfigurationData>>(
                        ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY
                    )
                        .get().clientSession.order?.orderId,
                    primerAccountId = sdk().resolve<BaseCacheDataSource<ConfigurationData, ConfigurationData>>(
                        ConfigurationCoreContainer.CACHED_CONFIGURATION_DI_KEY
                    )
                        .get().primerAccountId
                )
            }
        }

        sdk().resolve<CacheClientTokenDataSource>(
            dependencyName = ClientTokenCoreContainer.CACHE_CLIENT_TOKEN_DATA_SOURCE_DI_KEY
        ).update(config.clientTokenBase64.orEmpty())
    }
}
