package io.primer.android.components.analytics.internal

import io.primer.android.configuration.data.model.Environment
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class AnalyticsEnvironmentUrlProviderTest {

    @Test
    fun `getAnalyticsUrl should return DEV URL for DEV environment`() {
        val url = AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(Environment.DEV)

        assertEquals("https://analytics.dev.data.primer.io/v1/sdk-analytic-events", url)
    }

    @Test
    fun `getAnalyticsUrl should return STAGING URL for STAGING environment`() {
        val url = AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(Environment.STAGING)

        assertEquals("https://analytics.staging.data.primer.io/v1/sdk-analytic-events", url)
    }

    @Test
    fun `getAnalyticsUrl should return SANDBOX URL for SANDBOX environment`() {
        val url = AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(Environment.SANDBOX)

        assertEquals("https://analytics.sandbox.data.primer.io/v1/sdk-analytic-events", url)
    }

    @Test
    fun `getAnalyticsUrl should return PRODUCTION URL for PRODUCTION environment`() {
        val url = AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(Environment.PRODUCTION)

        assertEquals("https://analytics.production.data.primer.io/v1/sdk-analytic-events", url)
    }

    @Test
    fun `getAnalyticsUrl should return DEV URL for LOCAL_DOCKER environment`() {
        val url = AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(Environment.LOCAL_DOCKER)

        assertEquals("https://analytics.dev.data.primer.io/v1/sdk-analytic-events", url)
    }

    @Test
    fun `getAnalyticsUrl should handle all Environment enum values`() {
        Environment.entries.forEach { environment ->
            val url = AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(environment)

            when (environment) {
                Environment.DEV, Environment.LOCAL_DOCKER ->
                    assertEquals("https://analytics.dev.data.primer.io/v1/sdk-analytic-events", url)
                Environment.STAGING ->
                    assertEquals("https://analytics.staging.data.primer.io/v1/sdk-analytic-events", url)
                Environment.SANDBOX ->
                    assertEquals("https://analytics.sandbox.data.primer.io/v1/sdk-analytic-events", url)
                Environment.PRODUCTION ->
                    assertEquals("https://analytics.production.data.primer.io/v1/sdk-analytic-events", url)
            }
        }
    }

    @Test
    fun `getLoggingUrl should return DEV URL for DEV environment`() {
        val url = AnalyticsEnvironmentUrlProvider.getLoggingUrl(Environment.DEV)

        assertEquals("https://analytics.dev.data.primer.io/v1/sdk-logs", url)
    }

    @Test
    fun `getLoggingUrl should return STAGING URL for STAGING environment`() {
        val url = AnalyticsEnvironmentUrlProvider.getLoggingUrl(Environment.STAGING)

        assertEquals("https://analytics.staging.data.primer.io/v1/sdk-logs", url)
    }

    @Test
    fun `getLoggingUrl should return SANDBOX URL for SANDBOX environment`() {
        val url = AnalyticsEnvironmentUrlProvider.getLoggingUrl(Environment.SANDBOX)

        assertEquals("https://analytics.sandbox.data.primer.io/v1/sdk-logs", url)
    }

    @Test
    fun `getLoggingUrl should return PRODUCTION URL for PRODUCTION environment`() {
        val url = AnalyticsEnvironmentUrlProvider.getLoggingUrl(Environment.PRODUCTION)

        assertEquals("https://analytics.production.data.primer.io/v1/sdk-logs", url)
    }

    @Test
    fun `getLoggingUrl should return DEV URL for LOCAL_DOCKER environment`() {
        val url = AnalyticsEnvironmentUrlProvider.getLoggingUrl(Environment.LOCAL_DOCKER)

        assertEquals("https://analytics.dev.data.primer.io/v1/sdk-logs", url)
    }

    @Test
    fun `getLoggingUrl should handle all Environment enum values`() {
        Environment.entries.forEach { environment ->
            val url = AnalyticsEnvironmentUrlProvider.getLoggingUrl(environment)

            when (environment) {
                Environment.DEV, Environment.LOCAL_DOCKER ->
                    assertEquals("https://analytics.dev.data.primer.io/v1/sdk-logs", url)
                Environment.STAGING ->
                    assertEquals("https://analytics.staging.data.primer.io/v1/sdk-logs", url)
                Environment.SANDBOX ->
                    assertEquals("https://analytics.sandbox.data.primer.io/v1/sdk-logs", url)
                Environment.PRODUCTION ->
                    assertEquals("https://analytics.production.data.primer.io/v1/sdk-logs", url)
            }
        }
    }
}
