package io.primer.checkout.orchestrator.data

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
import io.primer.android.analytics.data.helper.SdkTypeResolver
import io.primer.android.analytics.data.models.AnalyticsData
import io.primer.android.analytics.data.models.SdkIntegrationType
import io.primer.android.analytics.data.models.SdkType
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.Environment
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.PrimerSettings
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DefaultSdkContextProviderTest {

    @MockK
    lateinit var context: Context

    @MockK
    lateinit var configurationDataSource: CacheConfigurationDataSource

    @MockK
    lateinit var checkoutSessionIdProvider: BaseDataProvider<String>

    @MockK
    lateinit var applicationIdProvider: BaseDataProvider<String>

    @MockK
    lateinit var settings: PrimerSettings

    @MockK
    lateinit var analyticsDataProvider: BaseDataProvider<AnalyticsData>

    @MockK
    lateinit var contentResolver: ContentResolver

    private lateinit var provider: DefaultSdkContextProvider

    @BeforeEach
    fun setUp() {
        mockkConstructor(SdkTypeResolver::class)
        mockkStatic(Settings.Secure::class)

        every { anyConstructed<SdkTypeResolver>().resolve() } returns SdkType.ANDROID_NATIVE
        every { context.contentResolver } returns contentResolver
        every { Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) } returns DEVICE_ID

        every { configurationDataSource.get() } returns createConfigurationData()
        every { checkoutSessionIdProvider.provide() } returns CHECKOUT_SESSION_ID
        every { applicationIdProvider.provide() } returns APPLICATION_ID
        every { settings.sdkIntegrationType } returns SdkIntegrationType.HEADLESS
        every { settings.paymentHandling } returns PrimerPaymentHandling.AUTO
        every { analyticsDataProvider.provide() } returns createAnalyticsData(analyticsUrl = ANALYTICS_URL)

        provider = DefaultSdkContextProvider(
            context = context,
            configurationDataSource = configurationDataSource,
            checkoutSessionIdProvider = checkoutSessionIdProvider,
            applicationIdProvider = applicationIdProvider,
            settings = settings,
            analyticsDataProvider = analyticsDataProvider,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkConstructor(SdkTypeResolver::class)
        unmockkStatic(Settings.Secure::class)
    }

    @Test
    fun `provide should return JSON with sdk section`() {
        val result = JSONObject(provider.provide(PAYMENT_METHOD_TYPE))
        val sdk = result.getJSONObject("sdk")

        assertEquals(SdkType.ANDROID_NATIVE.name, sdk.getString("type"))
        assertEquals(SdkIntegrationType.HEADLESS.name, sdk.getString("integrationType"))
        assertEquals(PrimerPaymentHandling.AUTO.name, sdk.getString("paymentHandling"))
        assertTrue(sdk.has("version"))
    }

    @Test
    fun `provide should return JSON with device section`() {
        val result = JSONObject(provider.provide(PAYMENT_METHOD_TYPE))

        assertTrue(result.has("device"))
        val device = result.getJSONObject("device")
        assertTrue(device.has("locale"))
    }

    @Test
    fun `provide should return JSON with app section`() {
        val result = JSONObject(provider.provide(PAYMENT_METHOD_TYPE))
        val app = result.getJSONObject("app")

        assertEquals(APPLICATION_ID, app.getString("identifier"))
    }

    @Test
    fun `provide should return JSON with session section`() {
        val result = JSONObject(provider.provide(PAYMENT_METHOD_TYPE))
        val session = result.getJSONObject("session")

        assertEquals(CHECKOUT_SESSION_ID, session.getString("checkoutSessionId"))
        assertEquals(CLIENT_SESSION_ID, session.getString("clientSessionId"))
        assertEquals(CUSTOMER_ID, session.getString("customerId"))
    }

    @Test
    fun `provide should return JSON with payment section`() {
        val result = JSONObject(provider.provide(PAYMENT_METHOD_TYPE))
        val payment = result.getJSONObject("payment")

        assertEquals(PAYMENT_METHOD_TYPE, payment.getString("paymentMethodType"))
    }

    @Test
    fun `provide should return JSON with merchant section`() {
        val result = JSONObject(provider.provide(PAYMENT_METHOD_TYPE))
        val merchant = result.getJSONObject("merchant")

        assertEquals(PRIMER_ACCOUNT_ID, merchant.getString("primerAccountId"))
    }

    @Test
    fun `provide should include analytics section when analyticsUrl is present`() {
        val result = JSONObject(provider.provide(PAYMENT_METHOD_TYPE))
        val analytics = result.getJSONObject("analytics")

        assertEquals(ANALYTICS_URL, analytics.getString("url"))
    }

    @Test
    fun `provide should omit analytics section when analyticsUrl is null`() {
        every { analyticsDataProvider.provide() } returns createAnalyticsData(analyticsUrl = null)

        val result = JSONObject(provider.provide(PAYMENT_METHOD_TYPE))

        assertFalse(result.has("analytics"))
    }

    @Test
    fun `provide should omit customerId when null`() {
        every { configurationDataSource.get() } returns createConfigurationData(
            customerId = null,
            primerAccountId = null,
        )

        val result = JSONObject(provider.provide(PAYMENT_METHOD_TYPE))
        val session = result.getJSONObject("session")

        assertFalse(session.has("customerId"))
    }

    private fun createConfigurationData(
        customerId: String? = CUSTOMER_ID,
        primerAccountId: String? = PRIMER_ACCOUNT_ID,
    ) = ConfigurationData(
        pciUrl = "",
        coreUrl = "",
        binDataUrl = "",
        assetsUrl = "",
        paymentMethods = emptyList(),
        keys = null,
        clientSession = ClientSessionDataResponse(
            clientSessionId = CLIENT_SESSION_ID,
            customerId = customerId,
            orderId = null,
            testId = null,
            amount = null,
            currencyCode = null,
            customer = null,
            order = null,
            paymentMethod = null,
        ),
        environment = Environment.PRODUCTION,
        primerAccountId = primerAccountId,
        iconsDisplayMetadata = emptyList(),
    )

    private fun createAnalyticsData(analyticsUrl: String?) = AnalyticsData(
        sdkIntegrationType = null,
        paymentHandling = null,
        analyticsUrl = analyticsUrl,
        clientSessionId = null,
        orderId = null,
        primerAccountId = null,
    )

    private companion object {
        const val PAYMENT_METHOD_TYPE = "ADYEN_IDEAL"
        const val DEVICE_ID = "test-device-id"
        const val CLIENT_SESSION_ID = "client-session-123"
        const val CUSTOMER_ID = "customer-456"
        const val CHECKOUT_SESSION_ID = "checkout-session-789"
        const val APPLICATION_ID = "com.example.app"
        const val PRIMER_ACCOUNT_ID = "primer-account-123"
        const val ANALYTICS_URL = "https://analytics.example.com"
    }
}
