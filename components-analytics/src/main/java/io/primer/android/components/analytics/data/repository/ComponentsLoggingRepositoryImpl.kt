package io.primer.android.components.analytics.data.repository

import android.content.Context
import io.primer.android.analytics.data.datasource.CheckoutSessionIdProvider
import io.primer.android.analytics.infrastructure.datasource.connectivity.UncaughtHandlerDataSource
import io.primer.android.components.analytics.data.model.EmptyResponse
import io.primer.android.components.analytics.data.model.LogEvent
import io.primer.android.components.analytics.data.model.LogMessageObject
import io.primer.android.components.analytics.data.model.PrimerInfo
import io.primer.android.components.analytics.data.model.SessionMetadata
import io.primer.android.components.analytics.data.provider.AppMetadataCollector
import io.primer.android.components.analytics.data.provider.DeviceInfoCollector
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@Suppress("LongParameterList")
internal class ComponentsLoggingRepositoryImpl(
    private val context: Context,
    private val logReporter: LogReporter,
    private val checkoutSessionIdProvider: CheckoutSessionIdProvider,
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val primerConfig: PrimerConfig,
    private val httpClient: PrimerHttpClient,
    private val deviceInfoCollector: DeviceInfoCollector,
    private val appMetadataCollector: AppMetadataCollector,
    private val uncaughtHandlerDataSource: UncaughtHandlerDataSource,
    private val sessionMetadata: SessionMetadata? = null,
) : ComponentsLoggingRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val configurationData: ConfigurationData by lazy {
        configurationDataSource.get()
    }

    private val loggingUrl: String by lazy {
        AnalyticsEnvironmentUrlProvider.getLoggingUrl(configurationData.environment)
    }

    override fun observeCrashes() {
        logReporter.debug("ComponentsLoggingRepository observing crash handler", component = "UncaughtHandler")
        uncaughtHandlerDataSource.execute(Unit)
            .onEach { crashProperties ->
                val fullStackTrace = crashProperties.stacktrace.joinToString("\n")
                val logMessageObject = LogMessageObject(
                    message = "SDK crash detected",
                    status = "error",
                    errorMessage = "Uncaught exception in SDK code",
                    errorStack = fullStackTrace,
                    primer = PrimerInfo(
                        checkoutSessionId = checkoutSessionIdProvider.provide(),
                        clientSessionId = configurationData.clientSession.clientSessionId,
                        primerAccountId = configurationData.primerAccountId,
                        customerId = configurationData.clientSession.customerId,
                    ),
                    deviceInfo = deviceInfoCollector.collect(),
                    appMetadata = appMetadataCollector.collect(),
                    sessionMetadata = sessionMetadata,
                )

                logReporter.error("Sending crash log to $loggingUrl", component = "UncaughtHandler")
                sendCrashLog(logMessageObject)
            }
            .catch { error ->
                logReporter.error("Failed to process crash: ${error.message}", component = "UncaughtHandler")
            }
            .launchIn(scope)
    }

    override fun sendInfoLog(
        message: String,
        event: String,
        initDurationMs: Long?,
    ) {
        // Skip sending logs in debug builds to avoid polluting production analytics
        if (BuildConfig.DEBUG) {
            return
        }

        httpClient.post<LogEvent, EmptyResponse>(
            url = loggingUrl,
            request = LogEvent(
                message = LogMessageObject(
                    message = message,
                    status = "info",
                    event = event,
                    initDurationMs = initDurationMs,
                    primer = PrimerInfo(
                        checkoutSessionId = checkoutSessionIdProvider.provide(),
                        clientSessionId = configurationData.clientSession.clientSessionId,
                        primerAccountId = configurationData.primerAccountId,
                        customerId = configurationData.clientSession.customerId,
                    ),
                    deviceInfo = deviceInfoCollector.collect(),
                    appMetadata = appMetadataCollector.collect(),
                    sessionMetadata = sessionMetadata,
                ).toJsonString(),
                hostname = context.packageName,
                service = SERVICE_NAME,
                ddsource = DD_SOURCE,
                ddtags = "env:${configurationData.environment.name},version:${BuildConfig.SDK_VERSION_STRING}",
            ),
            headers = mapOf(
                AUTHORIZATION_HEADER to "$BEARER_PREFIX ${primerConfig.clientTokenBase64 ?: ""}",
            ),
        ).flowOn(Dispatchers.IO).launchIn(scope)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun sendCrashLog(logMessageObject: LogMessageObject) {
        // Skip sending crash logs in debug builds to avoid polluting production analytics
        if (BuildConfig.DEBUG) {
            return
        }

        scope.launch {
            try {
                withTimeout(CRASH_LOG_TIMEOUT_MS) {
                    httpClient.suspendPost<LogEvent, EmptyResponse>(
                        url = loggingUrl,
                        request = LogEvent(
                            message = logMessageObject.toJsonString(),
                            hostname = context.packageName,
                            service = SERVICE_NAME,
                            ddsource = DD_SOURCE,
                            ddtags = "env:${configurationData.environment.name}," +
                                "version:${BuildConfig.SDK_VERSION_STRING}",
                        ),
                        headers = mapOf(
                            AUTHORIZATION_HEADER to "$BEARER_PREFIX ${primerConfig.clientTokenBase64 ?: ""}",
                        ),
                    )
                }
            } catch (e: Exception) {
                logReporter.debug("Crash log send failed (best effort): ${e.message}", component = "UncaughtHandler")
            }
        }
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer"
        private const val SERVICE_NAME = "android-sdk"
        private const val DD_SOURCE = "lambda"
        private const val CRASH_LOG_TIMEOUT_MS = 2000L
    }
}
