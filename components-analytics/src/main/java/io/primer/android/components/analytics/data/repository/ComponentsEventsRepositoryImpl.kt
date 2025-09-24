package io.primer.android.components.analytics.data.repository

import io.primer.android.components.analytics.data.model.AnalyticsEvent
import io.primer.android.components.analytics.data.model.AnalyticsResponse
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.provider.DeviceInfoProvider
import io.primer.android.components.analytics.internal.AnalyticsEnvironmentUrlProvider
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.Environment
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.logging.internal.LogReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID

internal class ComponentsEventsRepositoryImpl(
    private val logReporter: LogReporter,
    private val environment: Environment,
    private val configurationData: ConfigurationData,
    private val checkoutSessionId: String,
    private val sdkVersion: String,
    private val httpClient: PrimerHttpClient,
    private val clientToken: String,
    private val deviceInfoProvider: DeviceInfoProvider,
) : ComponentsEventsRepository {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val analyticsUrl: String by lazy {
        AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(environment)
    }

    override fun send(event: EventType, timestamp: Long) {
        val analyticsEvent = AnalyticsEvent(
            id = UUID.randomUUID().toString(),
            eventName = event.value,
            timeInSeconds = timestamp / 1000,
            checkoutSessionId = checkoutSessionId,
            clientSessionId = configurationData.clientSession.clientSessionId ?: "",
            sdkVersion = sdkVersion,
            primerAccountId = configurationData.primerAccountId ?: "",
            device = deviceInfoProvider.getDevice(),
            deviceType = deviceInfoProvider.getDeviceType(),
            userLocale = deviceInfoProvider.getUserLocale(),
        )

        // Fire-and-forget with proper error handling
        httpClient.post<AnalyticsEvent, AnalyticsResponse>(
            url = analyticsUrl,
            request = analyticsEvent,
            headers = mapOf(
                AUTHORIZATION_HEADER to "$BEARER_PREFIX $clientToken",
            ),
        )
            .flowOn(Dispatchers.IO)
            .onEach { response ->
                logReporter.debug("Analytics event sent successfully: ${event.value}, result: ${response.body.result}")
            }
            .catch { error ->
                logReporter.debug("Failed to send analytics event '${event.value}': ${error.message}")
            }
            .launchIn(scope)
    }
}
