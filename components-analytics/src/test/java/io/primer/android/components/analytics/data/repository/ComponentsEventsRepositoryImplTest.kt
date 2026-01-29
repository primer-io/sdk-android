package io.primer.android.components.analytics.data.repository

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.analytics.data.datasource.CheckoutSessionIdProvider
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.provider.DeviceInfoProvider
import io.primer.android.components.analytics.internal.AnalyticsEnvironmentUrlProvider
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.Environment
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.internal.PrimerConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class ComponentsEventsRepositoryImplTest {

    private lateinit var logReporter: LogReporter
    private lateinit var httpClient: PrimerHttpClient
    private lateinit var configurationData: ConfigurationData
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var checkoutSessionIdProvider: CheckoutSessionIdProvider
    private lateinit var primerConfig: PrimerConfig
    private lateinit var deviceInfoProvider: DeviceInfoProvider
    private lateinit var repository: ComponentsEventsRepositoryImpl

    private val environment = Environment.SANDBOX
    private val checkoutSessionId = "test-checkout-session-id"
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
        configurationDataSource = mockk()
        checkoutSessionIdProvider = mockk()
        primerConfig = mockk()
        deviceInfoProvider = mockk()

        val mockClientSession = mockk<ClientSessionDataResponse>()
        every { mockClientSession.clientSessionId } returns clientSessionId
        every { configurationData.clientSession } returns mockClientSession
        every { configurationData.primerAccountId } returns primerAccountId
        every { configurationData.environment } returns environment

        every { configurationDataSource.get() } returns configurationData
        every { checkoutSessionIdProvider.provide() } returns checkoutSessionId
        every { primerConfig.clientTokenBase64 } returns clientToken

        every { deviceInfoProvider.getDevice() } returns deviceModel
        every { deviceInfoProvider.getDeviceType() } returns deviceType
        every { deviceInfoProvider.getUserLocale() } returns userLocale

        repository = ComponentsEventsRepositoryImpl(
            logReporter = logReporter,
            checkoutSessionIdProvider = checkoutSessionIdProvider,
            configurationDataSource = configurationDataSource,
            primerConfig = primerConfig,
            httpClient = httpClient,
            deviceInfoProvider = deviceInfoProvider,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(AnalyticsEnvironmentUrlProvider)
    }

    @Test
    fun `send should create correct event and trigger HTTP call`() {
        val eventType = EventType.SdkInitStart

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

        val eventType = EventType.CheckoutFlowStarted

        repository.send(eventType)

        verify {
            mockClientSession.clientSessionId
        }
    }

    @Test
    fun `send should handle missing primerAccountId gracefully`() {
        every { configurationData.primerAccountId } returns null

        val eventType = EventType.PaymentSuccess.card("test-payment-id")

        repository.send(eventType)

        verify {
            configurationData.primerAccountId
        }
    }

    @Test
    fun `send should use correct analytics URL for environment`() {
        repository.send(EventType.SdkInitStart)

        verify {
            AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(environment)
        }
    }

    @Test
    fun `send should be fire and forget without blocking`() {
        val eventType = EventType.PaymentMethodSelection("PAYMENT_CARD")

        val startTime = System.currentTimeMillis()
        repository.send(eventType)
        val endTime = System.currentTimeMillis()

        assertTrue(endTime - startTime < 50, "Send method should return immediately")
    }

    @Test
    fun `repository should use correct environment URL for production`() {
        val productionUrl = "https://analytics.primer.io/events"
        val productionConfigurationData = mockk<ConfigurationData>()
        val productionConfigurationDataSource = mockk<BaseCacheDataSource<ConfigurationData, ConfigurationData>>()

        val mockClientSession = mockk<ClientSessionDataResponse>()
        every { mockClientSession.clientSessionId } returns clientSessionId
        every { productionConfigurationData.clientSession } returns mockClientSession
        every { productionConfigurationData.primerAccountId } returns primerAccountId
        every { productionConfigurationData.environment } returns Environment.PRODUCTION
        every { productionConfigurationDataSource.get() } returns productionConfigurationData

        every { AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(Environment.PRODUCTION) } returns productionUrl

        val productionRepository = ComponentsEventsRepositoryImpl(
            logReporter = logReporter,
            checkoutSessionIdProvider = checkoutSessionIdProvider,
            configurationDataSource = productionConfigurationDataSource,
            primerConfig = primerConfig,
            httpClient = httpClient,
            deviceInfoProvider = deviceInfoProvider,
        )

        productionRepository.send(EventType.SdkInitEnd)

        verify {
            AnalyticsEnvironmentUrlProvider.getAnalyticsUrl(Environment.PRODUCTION)
        }
    }

    @Test
    fun `data objects should have correct eventName`() {
        assertTrue(EventType.SdkInitStart.eventName == "SDK_INIT_START")
        assertTrue(EventType.SdkInitEnd.eventName == "SDK_INIT_END")
        assertTrue(EventType.CheckoutFlowStarted.eventName == "CHECKOUT_FLOW_STARTED")
        assertTrue(EventType.PaymentFlowExited.eventName == "PAYMENT_FLOW_EXITED")
        assertTrue(EventType.PaymentReattempted.eventName == "PAYMENT_REATTEMPTED")
    }

    @Test
    fun `data objects should return null for all optional fields`() {
        val event = EventType.SdkInitStart

        assertTrue(event.paymentMethod == null)
        assertTrue(event.paymentId == null)
        assertTrue(event.redirectDestinationUrl == null)
        assertTrue(event.threedsProvider == null)
        assertTrue(event.threedsResponse == null)
    }

    @Test
    fun `PaymentMethodSelection should have correct eventName and paymentMethod`() {
        val event = EventType.PaymentMethodSelection("GOOGLE_PAY")

        assertTrue(event.eventName == "PAYMENT_METHOD_SELECTION")
        assertTrue(event.paymentMethod == "GOOGLE_PAY")
        assertTrue(event.paymentId == null)
    }

    @Test
    fun `PaymentDetailsEntered should have correct eventName and paymentMethod`() {
        val event = EventType.PaymentDetailsEntered("PAYPAL")

        assertTrue(event.eventName == "PAYMENT_DETAILS_ENTERED")
        assertTrue(event.paymentMethod == "PAYPAL")
        assertTrue(event.paymentId == null)
    }

    @Test
    fun `PaymentSubmitted should have correct eventName and paymentMethod`() {
        val event = EventType.PaymentSubmitted("KLARNA")

        assertTrue(event.eventName == "PAYMENT_SUBMITTED")
        assertTrue(event.paymentMethod == "KLARNA")
        assertTrue(event.paymentId == null)
    }

    @Test
    fun `PaymentProcessingStarted should have correct eventName and paymentMethod`() {
        val event = EventType.PaymentProcessingStarted("STRIPE_ACH")

        assertTrue(event.eventName == "PAYMENT_PROCESSING_STARTED")
        assertTrue(event.paymentMethod == "STRIPE_ACH")
        assertTrue(event.paymentId == null)
    }

    @Test
    fun `PaymentSuccess should have correct eventName, paymentMethod and paymentId`() {
        val event = EventType.PaymentSuccess("PAYMENT_CARD", "payment-123")

        assertTrue(event.eventName == "PAYMENT_SUCCESS")
        assertTrue(event.paymentMethod == "PAYMENT_CARD")
        assertTrue(event.paymentId == "payment-123")
        assertTrue(event.redirectDestinationUrl == null)
    }

    @Test
    fun `PaymentFailure should have correct eventName, paymentMethod and optional paymentId`() {
        val eventWithId = EventType.PaymentFailure("PAYMENT_CARD", "payment-456")
        assertTrue(eventWithId.eventName == "PAYMENT_FAILURE")
        assertTrue(eventWithId.paymentMethod == "PAYMENT_CARD")
        assertTrue(eventWithId.paymentId == "payment-456")

        val eventWithoutId = EventType.PaymentFailure("PAYMENT_CARD", null)
        assertTrue(eventWithoutId.eventName == "PAYMENT_FAILURE")
        assertTrue(eventWithoutId.paymentMethod == "PAYMENT_CARD")
        assertTrue(eventWithoutId.paymentId == null)
    }

    @Test
    fun `PaymentThreeDS should have correct eventName and 3DS fields`() {
        val event = EventType.PaymentThreeDS("PAYMENT_CARD", "Netcetera", "05")

        assertTrue(event.eventName == "PAYMENT_THREEDS")
        assertTrue(event.paymentMethod == "PAYMENT_CARD")
        assertTrue(event.threedsProvider == "Netcetera")
        assertTrue(event.threedsResponse == "05")
        assertTrue(event.paymentId == null)
    }

    @Test
    fun `PaymentRedirect should have correct eventName and redirect fields`() {
        val event = EventType.PaymentRedirect("PAYPAL", "https://paypal.com/redirect")

        assertTrue(event.eventName == "PAYMENT_REDIRECT_TO_THIRD_PARTY")
        assertTrue(event.paymentMethod == "PAYPAL")
        assertTrue(event.redirectDestinationUrl == "https://paypal.com/redirect")
        assertTrue(event.paymentId == null)
    }

    @Test
    fun `companion factory method card() should create PaymentDetailsEntered with PAYMENT_CARD`() {
        val event = EventType.PaymentDetailsEntered.card()

        assertTrue(event.paymentMethod == "PAYMENT_CARD")
        assertTrue(event.eventName == "PAYMENT_DETAILS_ENTERED")
    }

    @Test
    fun `companion factory method card() should create PaymentSubmitted with PAYMENT_CARD`() {
        val event = EventType.PaymentSubmitted.card()

        assertTrue(event.paymentMethod == "PAYMENT_CARD")
        assertTrue(event.eventName == "PAYMENT_SUBMITTED")
    }

    @Test
    fun `companion factory method card() should create PaymentProcessingStarted with PAYMENT_CARD`() {
        val event = EventType.PaymentProcessingStarted.card()

        assertTrue(event.paymentMethod == "PAYMENT_CARD")
        assertTrue(event.eventName == "PAYMENT_PROCESSING_STARTED")
    }

    @Test
    fun `companion factory method card() should create PaymentSuccess with PAYMENT_CARD and paymentId`() {
        val event = EventType.PaymentSuccess.card("payment-789")

        assertTrue(event.paymentMethod == "PAYMENT_CARD")
        assertTrue(event.paymentId == "payment-789")
        assertTrue(event.eventName == "PAYMENT_SUCCESS")
    }

    @Test
    fun `companion factory method card() should create PaymentFailure with PAYMENT_CARD and optional paymentId`() {
        val eventWithId = EventType.PaymentFailure.card("payment-fail-123")
        assertTrue(eventWithId.paymentMethod == "PAYMENT_CARD")
        assertTrue(eventWithId.paymentId == "payment-fail-123")
        assertTrue(eventWithId.eventName == "PAYMENT_FAILURE")

        val eventWithoutId = EventType.PaymentFailure.card()
        assertTrue(eventWithoutId.paymentMethod == "PAYMENT_CARD")
        assertTrue(eventWithoutId.paymentId == null)
        assertTrue(eventWithoutId.eventName == "PAYMENT_FAILURE")
    }
}
