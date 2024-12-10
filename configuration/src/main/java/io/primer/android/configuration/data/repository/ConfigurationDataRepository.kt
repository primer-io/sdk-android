package io.primer.android.configuration.data.repository

import android.net.Uri
import io.primer.android.analytics.data.models.CacheSourceAnalyticsContext
import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerProperties
import io.primer.android.analytics.data.models.TimerType
import io.primer.android.configuration.PrimerSessionConstants
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.datasource.ConfigurationCache
import io.primer.android.configuration.data.datasource.GlobalCacheConfigurationCacheDataSource
import io.primer.android.configuration.data.datasource.RemoteConfigurationDataSource
import io.primer.android.configuration.data.datasource.RemoteConfigurationResourcesDataSource
import io.primer.android.configuration.data.model.ConfigurationDataResponse
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.model.Configuration
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.data.network.PrimerResponse
import io.primer.android.core.extensions.buildWithQueryParams
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.core.utils.EventFlowProvider
import io.primer.android.data.configuration.models.ConfigurationSource
import kotlinx.coroutines.yield
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.TimeSource

internal class ConfigurationDataRepository(
    private val remoteConfigurationDataSource: RemoteConfigurationDataSource,
    private val remoteConfigurationResourcesDataSource: RemoteConfigurationResourcesDataSource,
    private val localConfigurationDataSource: CacheConfigurationDataSource,
    private val configurationUrlProvider: BaseDataProvider<String>,
    private val clientTokenProvider: BaseDataProvider<String>,
    private val globalConfigurationCache: GlobalCacheConfigurationCacheDataSource,
    private val timerEventProvider: EventFlowProvider<TimerProperties>,
    private val logReporter: LogReporter,
    private val getCurrentTimeMillis: () -> Long = { System.currentTimeMillis() }
) : ConfigurationRepository {

    override suspend fun fetchConfiguration(cachePolicy: CachePolicy): Result<Configuration> = runSuspendCatching {
        when (cachePolicy) {
            is CachePolicy.ForceCache -> localConfigurationDataSource.get().toConfiguration()

            is CachePolicy.ForceNetwork -> getAndCacheConfiguration()

            is CachePolicy.CacheFirst -> {
                val configurationDataCache = globalConfigurationCache.get()
                val isValid = configurationDataCache?.first?.let { cache ->
                    cache.clientToken == clientTokenProvider.provide() &&
                        cache.validUntil >= getCurrentTimeMillis()
                } ?: false
                when (isValid) {
                    true -> {
                        val timeSource = TimeSource.Monotonic
                        val start = timeSource.markNow()
                        logConfigurationLoadingEvent(
                            timerType = TimerType.START,
                            duration = timeSource.markNow() - start,
                            analyticsContext = CacheSourceAnalyticsContext(source = ConfigurationSource.CACHE.name)
                        )
                        val configurationResponse = requireNotNull(configurationDataCache?.second)

                        logConfigurationLoadingEvent(
                            timerType = TimerType.END,
                            duration = timeSource.markNow() - start,
                            analyticsContext = CacheSourceAnalyticsContext(source = ConfigurationSource.CACHE.name)
                        )

                        remoteConfigurationResourcesDataSource.execute(configurationResponse.paymentMethods)
                            .let {
                                configurationResponse.toConfigurationData(it)
                            }.also { configurationData ->
                                localConfigurationDataSource.update(configurationData)
                            }.toConfiguration()
                    }

                    false -> getAndCacheConfiguration()
                }
            }
        }
    }

    override fun getConfiguration(): Configuration =
        localConfigurationDataSource.get().toConfiguration()

    private suspend fun getAndCacheConfiguration(): Configuration {
        yield()
        val timeSource = TimeSource.Monotonic
        val start = timeSource.markNow()
        globalConfigurationCache.clear()
        logConfigurationLoadingEvent(
            timerType = TimerType.START,
            duration = timeSource.markNow() - start,
            analyticsContext = CacheSourceAnalyticsContext(source = ConfigurationSource.NETWORK.name)
        )
        val configurationResponse = remoteConfigurationDataSource.execute(
            Uri.parse(configurationUrlProvider.provide())
                .buildWithQueryParams(mapOf(DISPLAY_METADATA_QUERY_KEY to true))
        )

        yield()
        updateGlobalConfigurationCache(configurationResponse)

        yield()
        val configurationData =
            remoteConfigurationResourcesDataSource.execute(configurationResponse.body.paymentMethods).let {
                configurationResponse.body.toConfigurationData(it)
            }.also { configurationData ->
                yield()
                localConfigurationDataSource.update(configurationData)
            }

        logConfigurationLoadingEvent(
            timerType = TimerType.END,
            duration = timeSource.markNow() - start,
            analyticsContext = CacheSourceAnalyticsContext(source = ConfigurationSource.NETWORK.name)
        )

        return configurationData.toConfiguration()
    }

    private fun updateGlobalConfigurationCache(configurationResponse: PrimerResponse<ConfigurationDataResponse>) {
        val ttlValue = configurationResponse.headers[PrimerSessionConstants.PRIMER_SESSION_CACHE_TTL_HEADER]
            ?.firstOrNull()?.toLongOrNull()
            ?: PrimerSessionConstants.DEFAULT_SESSION_TTL_VALUE
        globalConfigurationCache.update(
            ConfigurationCache(
                validUntil = TimeUnit.SECONDS.toMillis(ttlValue) + getCurrentTimeMillis(),
                clientToken = clientTokenProvider.provide()
            ) to configurationResponse.body
        )
    }

    private suspend fun logConfigurationLoadingEvent(
        timerType: TimerType,
        duration: Duration,
        analyticsContext: CacheSourceAnalyticsContext
    ) = timerEventProvider.getEventProvider().emit(
        TimerProperties(
            id = TimerId.CONFIGURATION_LOADING,
            timerType = timerType,
            duration = duration.inWholeMilliseconds,
            analyticsContext = analyticsContext
        )
    )

    private companion object {
        const val DISPLAY_METADATA_QUERY_KEY = "withDisplayMetadata"
    }
}
