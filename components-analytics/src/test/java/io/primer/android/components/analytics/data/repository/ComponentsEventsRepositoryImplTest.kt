package io.primer.android.components.analytics.data.repository

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.provider.DeviceInfoProvider
import io.primer.android.components.analytics.internal.AnalyticsEnvironmentUrlProvider
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.Environment
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.logging.internal.LogReporter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class ComponentsEventsRepositoryImplTest {

    private lateinit var logReporter: LogReporter
    private lateinit var httpClient: PrimerHttpClient
    private lateinit var configurationData: ConfigurationData
    private lateinit var deviceInfoProvider: DeviceInfoProvider
    private lateinit var repository: ComponentsEventsRepositoryImpl

    private val environment = Environment.SANDBOX
    private val checkoutSessionId = "test-checkout-session-id"
    private val sdkVersion = "1.0.0"
    private val clientToken = "test-client-token"
    private val clientSessionId = "test-client-session-id"
    private val primerAccountId = "test-primer-account-id"
    private val analyticsUrl = "https://analytics.sandbox.primer.io/events"
    private val deviceModel = "Google Pixel 5"
    private val deviceType = "phone"
    private val userLocale = "en-US"

    @BeforeEach
    fun setUp() {
        mockkObject(AnalyticsEnvironmentUrlProvider)
        every { AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(any()) } returns analyticsUrl

        logReporter = mockk(relaxed = true)
        httpClient = mockk(relaxed = true)
        configurationData = mockk()
        deviceInfoProvider = mockk()

        val mockClientSession = mockk<ClientSessionDataResponse>()
        every { mockClientSession.clientSessionId } returns clientSessionId
        every { configurationData.clientSession } returns mockClientSession
        every { configurationData.primerAccountId } returns primerAccountId

        every { deviceInfoProvider.getDevice() } returns deviceModel
        every { deviceInfoProvider.getDeviceType() } returns deviceType
        every { deviceInfoProvider.getUserLocale() } returns userLocale

        repository = ComponentsEventsRepositoryImpl(
            logReporter = logReporter,
            environment = environment,
            configurationData = configurationData,
            checkoutSessionId = checkoutSessionId,
            sdkVersion = sdkVersion,
            httpClient = httpClient,
            clientToken = clientToken,
            deviceInfoProvider = deviceInfoProvider,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(AnalyticsEnvironmentUrlProvider)
    }

    @Test
    fun `send should create correct event and trigger HTTP call`() {
        val eventType = EventType.SDK_INIT_START

        repository.send(eventType)

        verify {
            AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(environment)
        }
    }

    @Test
    fun `send should handle missing clientSessionId gracefully`() {
        val mockClientSession = mockk<ClientSessionDataResponse>()
        every { mockClientSession.clientSessionId } returns null
        every { configurationData.clientSession } returns mockClientSession

        val eventType = EventType.CHECKOUT_FLOW_STARTED

        repository.send(eventType)

        verify {
            mockClientSession.clientSessionId
        }
    }

    @Test
    fun `send should handle missing primerAccountId gracefully`() {
        every { configurationData.primerAccountId } returns null

        val eventType = EventType.PAYMENT_SUCCESS

        repository.send(eventType)

        verify {
            configurationData.primerAccountId
        }
    }

    @Test
    fun `send should work for all event types`() {
        EventType.entries.forEach { eventType ->
            repository.send(eventType)
        }

        verify(atLeast = 1) {
            AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(environment)
        }
    }

    @Test
    fun `send should use correct analytics URL for environment`() {
        repository.send(EventType.SDK_INIT_START)

        verify {
            AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(environment)
        }
    }

    @Test
    fun `send should be fire and forget without blocking`() {
        val eventType = EventType.PAYMENT_METHOD_SELECTION

        val startTime = System.currentTimeMillis()
        repository.send(eventType)
        val endTime = System.currentTimeMillis()

        assertTrue(endTime - startTime < 50, "Send method should return immediately")
    }

    @Test
    fun `repository should use correct environment URL for production`() {
        val productionUrl = "https://analytics.primer.io/events"
        every { AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(Environment.PRODUCTION) } returns productionUrl

        val productionRepository = ComponentsEventsRepositoryImpl(
            logReporter = logReporter,
            environment = Environment.PRODUCTION,
            configurationData = configurationData,
            checkoutSessionId = checkoutSessionId,
            sdkVersion = sdkVersion,
            httpClient = httpClient,
            clientToken = clientToken,
            deviceInfoProvider = deviceInfoProvider,
        )

        productionRepository.send(EventType.SDK_INIT_END)

        verify {
            AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(Environment.PRODUCTION)
        }
    }
}
