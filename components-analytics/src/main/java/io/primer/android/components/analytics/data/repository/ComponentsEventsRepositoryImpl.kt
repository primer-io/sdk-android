package io.primer.android.components.analytics.data.repository

import io.primer.android.analytics.data.datasource.CheckoutSessionIdProvider
import io.primer.android.components.analytics.data.model.AnalyticsEvent
import io.primer.android.components.analytics.data.model.AnalyticsResponse
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.provider.DeviceInfoProvider
import io.primer.android.components.analytics.internal.AnalyticsEnvironmentUrlProvider
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.BuildConfig
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.internal.PrimerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID

@Suppress("LongParameterList")
internal class ComponentsEventsRepositoryImpl(
    private val logReporter: LogReporter,
    private val checkoutSessionIdProvider: CheckoutSessionIdProvider,
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val primerConfig: PrimerConfig,
    private val httpClient: PrimerHttpClient,
    private val deviceInfoProvider: DeviceInfoProvider,
) : ComponentsEventsRepository {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val configurationData: ConfigurationData by lazy {
        configurationDataSource.get()
    }

    private val analyticsUrl: String by lazy {
        AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(configurationData.environment)
    }

    override fun send(
        event: EventType,
        timestamp: Long,
    ) {

        if (BuildConfig.DEBUG) {
            return
        }

        val analyticsEvent = AnalyticsEvent(
            id = UUID.randomUUID().toString(),
            eventName = event.eventName,
            timeInSeconds = timestamp / 1000,
            checkoutSessionId = checkoutSessionIdProvider.provide(),
            clientSessionId = configurationData.clientSession.clientSessionId ?: "",
            sdkVersion = BuildConfig.SDK_VERSION_STRING,
            primerAccountId = configurationData.primerAccountId ?: "",
            device = deviceInfoProvider.getDevice(),
            deviceType = deviceInfoProvider.getDeviceType(),
            userLocale = deviceInfoProvider.getUserLocale(),
            paymentMethod = event.paymentMethod,
            paymentId = event.paymentId,
            redirectDestinationUrl = event.redirectDestinationUrl,
            threedsProvider = event.threedsProvider,
            threedsResponse = event.threedsResponse,
        )

        // Fire-and-forget with proper error handling
        httpClient.post<AnalyticsEvent, AnalyticsResponse>(
            url = analyticsUrl,
            request = analyticsEvent,
            headers = mapOf(
                AUTHORIZATION_HEADER to "$BEARER_PREFIX ${primerConfig.clientTokenBase64 ?: ""}",
            ),
        )
            .flowOn(Dispatchers.IO)
            .onEach { response ->
                logReporter.debug(
                    "Analytics event sent successfully: ${event.eventName}, result: ${response.body.result}",
                )
            }
            .catch { error ->
                logReporter.debug("Failed to send analytics event '${event.eventName}': ${error.message}")
            }
            .launchIn(scope)
    }
}
