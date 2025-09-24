package io.primer.android.components.analytics.internal

import io.primer.android.configuration.data.model.Environment

/**
 * This is a temporary implementation until we align on where to get the url from
 *
 * Provides analytics event URL based on the current environment configuration.
 */
internal object AnalyticsEnvironmentUrlProvider {

    fun getAnalyticsUrl(environment: Environment): String {
        return when (environment) {
            Environment.DEV -> "https://analytics.dev.data.primer.io/v1/sdk-analytic-events"
            Environment.STAGING -> "https://analytics.staging.data.primer.io/v1/sdk-analytic-events"
            Environment.SANDBOX -> "https://analytics.sandbox.data.primer.io/v1/sdk-analytic-events"
            Environment.PRODUCTION -> "https://analytics.production.data.primer.io/v1/sdk-analytic-events"
            Environment.LOCAL_DOCKER -> "https://analytics.dev.data.primer.io/v1/sdk-analytic-events"
        }
    }
}
