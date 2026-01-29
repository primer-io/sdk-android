package io.primer.android.components.analytics.internal

import io.primer.android.configuration.data.model.Environment

internal object AnalyticsEnvironmentUrlProvider {

    private const val ANALYTICS_PATH = "/v1/sdk-analytic-events"
    private const val LOGS_PATH = "/v1/sdk-logs"

    fun getAnalyticsUrl(environment: Environment): String {
        return "${getBaseUrl(environment)}$ANALYTICS_PATH"
    }

    fun getLoggingUrl(environment: Environment): String {
        return "${getBaseUrl(environment)}$LOGS_PATH"
    }

    private fun getBaseUrl(environment: Environment): String {
        return when (environment) {
            Environment.DEV -> "https://analytics.dev.data.primer.io"
            Environment.STAGING -> "https://analytics.staging.data.primer.io"
            Environment.SANDBOX -> "https://analytics.sandbox.data.primer.io"
            Environment.PRODUCTION -> "https://analytics.production.data.primer.io"
            Environment.LOCAL_DOCKER -> "https://analytics.dev.data.primer.io"
        }
    }
}
