package io.primer.android.analytics.data.helper

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.analytics.data.datasource.RemoteAnalyticsDataSource
import io.primer.android.analytics.data.models.AnalyticsDataRequest
import io.primer.android.analytics.data.models.AnalyticsEvent
import io.primer.android.core.data.model.BaseRemoteUrlRequest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnalyticsDataSenderTest {
    private val remoteAnalyticsDataSource = mockk<RemoteAnalyticsDataSource>()
    private val sender = AnalyticsDataSender(remoteAnalyticsDataSource)

    @Test
    fun `sendEvents posts each group to its own Primer analytics url`() =
        runTest {
            val sandboxEvent = event(SANDBOX_URL)
            val defaultEvent = event(null)
            every { remoteAnalyticsDataSource.execute(any()) } answers { flowOf(request().url) }

            val sent = sender.sendEvents(listOf(sandboxEvent, defaultEvent)).toList().flatten()

            assertEquals(setOf(sandboxEvent, defaultEvent), sent.toSet())
            verify(exactly = 1) {
                remoteAnalyticsDataSource.execute(match { it.url == SANDBOX_URL })
            }
            verify(exactly = 1) {
                remoteAnalyticsDataSource.execute(match { it.url == DEFAULT_URL })
            }
        }

    @Test
    fun `sendEvents reports events bound for a non-Primer host as sent without a request`() =
        runTest {
            val sandboxEvent = event(SANDBOX_URL)
            val forgedEvent = event("https://attacker.example/sdk-logs")
            val insecureEvent = event("http://analytics.sandbox.data.primer.io/sdk-logs")
            every { remoteAnalyticsDataSource.execute(any()) } answers { flowOf(request().url) }

            val sent = sender.sendEvents(listOf(forgedEvent, sandboxEvent, insecureEvent)).toList().flatten()

            assertEquals(setOf(sandboxEvent, forgedEvent, insecureEvent), sent.toSet())
            verify(exactly = 1) { remoteAnalyticsDataSource.execute(any()) }
            verify(exactly = 1) {
                remoteAnalyticsDataSource.execute(match { it.url == SANDBOX_URL })
            }
        }

    @Test
    fun `sendEvents makes no request when every event is bound for a non-Primer host`() =
        runTest {
            val forgedEvent = event("https://attacker.example/sdk-logs")

            val sent = sender.sendEvents(listOf(forgedEvent)).toList().flatten()

            assertEquals(listOf(forgedEvent), sent)
            verify(exactly = 0) { remoteAnalyticsDataSource.execute(any()) }
        }

    private fun event(url: String?): AnalyticsEvent =
        mockk {
            every { analyticsUrl } returns url
            every { withAnalyticsUrl(any()) } returns this
        }

    private fun io.mockk.MockKAnswerScope<*, *>.request() = firstArg<BaseRemoteUrlRequest<AnalyticsDataRequest>>()

    private companion object {
        const val SANDBOX_URL = "https://analytics.sandbox.data.primer.io/sdk-logs"
        const val DEFAULT_URL = "https://analytics.production.data.primer.io/sdk-logs"
    }
}
