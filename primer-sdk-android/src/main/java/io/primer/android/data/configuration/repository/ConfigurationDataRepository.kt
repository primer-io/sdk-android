package io.primer.android.data.configuration.repository

import android.net.Uri
import io.primer.android.analytics.data.helper.TimerEventProvider
import io.primer.android.analytics.data.models.CacheSourceAnalyticsContext
import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerProperties
import io.primer.android.analytics.data.models.TimerType
import io.primer.android.data.configuration.datasource.ConfigurationCache
import io.primer.android.data.configuration.datasource.GlobalConfigurationCacheDataSource
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.datasource.RemoteConfigurationDataSource
import io.primer.android.data.configuration.datasource.RemoteConfigurationResourcesDataSource
import io.primer.android.data.configuration.models.ConfigurationDataResponse
import io.primer.android.data.configuration.models.ConfigurationSource
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.data.utils.PrimerSessionConstants
import io.primer.android.domain.session.CachePolicy
import io.primer.android.domain.session.models.Configuration
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.http.PrimerResponse
import io.primer.android.utils.buildWithQueryParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.TimeSource

@Suppress("LongParameterList")
internal class ConfigurationDataRepository(
    private val remoteConfigurationDataSource: RemoteConfigurationDataSource,
    private val remoteConfigurationResourcesDataSource: RemoteConfigurationResourcesDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val localClientTokenDataSource: LocalClientTokenDataSource,
    private val globalConfigurationCache: GlobalConfigurationCacheDataSource,
    private val primerConfig: PrimerConfig,
    private val timerEventProvider: TimerEventProvider,
    private val getCurrentTimeMillis: () -> Long = { System.currentTimeMillis() }
) : ConfigurationRepository {

    override fun fetchConfiguration(cachePolicy: CachePolicy): Flow<Configuration> = when (cachePolicy) {
        is CachePolicy.ForceCache -> localConfigurationDataSource.get()
            .map { configurationData -> configurationData.toConfiguration() }

        is CachePolicy.ForceNetwork -> getAndCacheConfiguration()

        is CachePolicy.CacheFirst -> {
            val configurationDataCache = globalConfigurationCache.get()
            val isValid = configurationDataCache?.first?.let { cache ->
                cache.clientToken == primerConfig.clientTokenBase64 &&
                    cache.validUntil >= getCurrentTimeMillis.invoke()
            } ?: false
            when (isValid) {
                true -> {
                    val timeSource = TimeSource.Monotonic
                    val start = timeSource.markNow()
                    flowOf(requireNotNull(configurationDataCache?.second))
                        .onStart {
                            logConfigurationLoadingEvent(
                                timerType = TimerType.START,
                                duration = timeSource.markNow() - start,
                                analyticsContext = CacheSourceAnalyticsContext(source = ConfigurationSource.CACHE)
                            )
                        }.onEach {
                            logConfigurationLoadingEvent(
                                timerType = TimerType.END,
                                duration = timeSource.markNow() - start,
                                analyticsContext = CacheSourceAnalyticsContext(source = ConfigurationSource.CACHE)
                            )
                        }.flatMapLatest { configurationResponse ->
                            remoteConfigurationResourcesDataSource.execute(configurationResponse.paymentMethods).map {
                                configurationResponse.toConfigurationData(it)
                            }
                        }.onEach { configurationData ->
                            localConfigurationDataSource.update(configurationData)
                        }
                        .map { configurationData -> configurationData.toConfiguration() }
                }

                false -> getAndCacheConfiguration()
            }
        }
    }

    override fun getConfiguration(): Configuration =
        localConfigurationDataSource.getConfiguration().toConfiguration()

    private fun getAndCacheConfiguration(): Flow<Configuration> {
        val timeSource = TimeSource.Monotonic
        val start = timeSource.markNow()
        return remoteConfigurationDataSource.execute(
            Uri.parse(localClientTokenDataSource.get().configurationUrl.orEmpty())
                .buildWithQueryParams(mapOf(DISPLAY_METADATA_QUERY_KEY to true))
        ).onStart {
            globalConfigurationCache.clear()
            logConfigurationLoadingEvent(
                timerType = TimerType.START,
                duration = timeSource.markNow() - start,
                analyticsContext = CacheSourceAnalyticsContext(source = ConfigurationSource.NETWORK)
            )
        }.onEach { configurationResponse ->
            updateGlobalConfigurationCache(configurationResponse)
        }.flatMapLatest { configurationResponse ->
            remoteConfigurationResourcesDataSource.execute(configurationResponse.body.paymentMethods).map {
                configurationResponse.body.toConfigurationData(it)
            }
        }.onEach { configurationData ->
            localConfigurationDataSource.update(configurationData)
            logConfigurationLoadingEvent(
                timerType = TimerType.END,
                duration = timeSource.markNow() - start,
                analyticsContext = CacheSourceAnalyticsContext(source = ConfigurationSource.NETWORK)
            )
        }.map { configurationData ->
            configurationData.toConfiguration()
        }
    }

    private fun updateGlobalConfigurationCache(configurationResponse: PrimerResponse<ConfigurationDataResponse>) {
        val ttlValue = configurationResponse.headers[PrimerSessionConstants.PRIMER_SESSION_CACHE_TTL_HEADER]
            ?.firstOrNull()?.toLongOrNull()
            ?: PrimerSessionConstants.DEFAULT_SESSION_TTL_VALUE
        globalConfigurationCache.update(
            ConfigurationCache(
                validUntil = TimeUnit.SECONDS.toMillis(ttlValue) + getCurrentTimeMillis.invoke(),
                clientToken = primerConfig.clientTokenBase64.orEmpty()
            ) to configurationResponse.body
        )
    }

    private suspend fun logConfigurationLoadingEvent(
        timerType: TimerType,
        duration: Duration,
        analyticsContext: CacheSourceAnalyticsContext
    ) = timerEventProvider.getTimerEventProvider().emit(
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
