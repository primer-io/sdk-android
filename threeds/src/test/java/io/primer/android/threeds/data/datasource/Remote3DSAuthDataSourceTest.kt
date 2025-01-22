package io.primer.android.threeds.data.datasource

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.utils.PrimerTimeouts
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import io.primer.android.threeds.data.models.auth.BeginAuthDataRequest
import io.primer.android.threeds.data.models.postAuth.SuccessContinueAuthDataRequest
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class Remote3DSAuthDataSourceTest {
    @ParameterizedTest
    @EnumSource(value = PrimerApiVersion::class)
    fun `when get3dsAuthToken is called, request is made with correct endpoint, method, request body and headers`(
        apiVersion: PrimerApiVersion,
    ): Unit =
        runTest {
            val requestSlot = slot<Request>()
            val mockedOkHttpClient =
                mockk<OkHttpClient> {
                    coEvery { newCall(capture(requestSlot)) } returns mockk(relaxed = true)
                }
            val primerHttpClient =
                mockk<PrimerHttpClient> {
                    every { okHttpClient } returns mockedOkHttpClient
                    every { withTimeout(PRIMER_15S_TIMEOUT) } returns this
                }
            val tested = Remote3DSAuthDataSource(primerHttpClient) { apiVersion }
            val configuration =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "https://example.com"
                }
            val paymentMethodToken = "paymentMethodToken"
            val beginAuthRequest =
                mockk<BeginAuthDataRequest>(relaxed = true) {
                    every { device } returns mockk(relaxed = true)
                    every { customer } returns mockk(relaxed = true)
                }

            val job = launch { tested.get3dsAuthToken(configuration, paymentMethodToken, beginAuthRequest) }
            delay(1.seconds)
            job.cancel()

            verify {
                mockedOkHttpClient.newCall(any())
            }
            val request = requestSlot.captured
            assertEquals("POST", request.method)
            assertEquals("https://example.com/3ds/paymentMethodToken/auth", request.url.toString())
            val expectedSink = Buffer()
            primerHttpClient.getRequestBody(beginAuthRequest).writeTo(expectedSink)
            val actualSink = Buffer()
            request.body!!.writeTo(actualSink)
            assertContentEquals(expectedSink.readByteArray(), actualSink.readByteArray())
            assertEquals(apiVersion.toHeaderMap().toHeaders(), request.headers)
        }

    @Test
    fun `when get3dsAuthToken is called, response is processed when the server responds in time`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_15S_TIMEOUT } returns 200.milliseconds
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse().setHeadersDelay(100, TimeUnit.MILLISECONDS))
                    start()
                }
            val configuration =
                mockk<ConfigurationData> {
                    every { pciUrl } returns mockWebServer.url("/").toString()
                }
            val paymentMethodToken = "paymentMethodToken"
            val beginAuthRequest =
                mockk<BeginAuthDataRequest>(relaxed = true) {
                    every { device } returns mockk(relaxed = true)
                    every { customer } returns mockk(relaxed = true)
                }
            every { configuration.pciUrl } returns mockWebServer.url("/").toString()

            val tested =
                Remote3DSAuthDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                ) { PrimerApiVersion.LATEST }
            assertThrows<JsonDecodingException> {
                tested.get3dsAuthToken(
                    configuration,
                    paymentMethodToken,
                    beginAuthRequest,
                )
            }

            mockWebServer.shutdown()
            unmockkAll()
        }

    @Test
    fun `when get3dsAuthToken is called, SocketTimeoutException is thrown when the server takes too long to respond`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_15S_TIMEOUT } returns 100.milliseconds
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse().setHeadersDelay(200, TimeUnit.MILLISECONDS))
                    start()
                }
            val configuration =
                mockk<ConfigurationData> {
                    every { pciUrl } returns mockWebServer.url("/").toString()
                }
            val paymentMethodToken = "paymentMethodToken"
            val beginAuthRequest =
                mockk<BeginAuthDataRequest>(relaxed = true) {
                    every { device } returns mockk(relaxed = true)
                    every { customer } returns mockk(relaxed = true)
                }

            val tested =
                Remote3DSAuthDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                ) { PrimerApiVersion.LATEST }
            assertThrows<SocketTimeoutException> {
                tested.get3dsAuthToken(
                    configuration,
                    paymentMethodToken,
                    beginAuthRequest,
                )
            }

            mockWebServer.shutdown()
            unmockkAll()
        }

    @ParameterizedTest
    @EnumSource(value = PrimerApiVersion::class)
    fun `when continue3dsAuth is called, request is made with correct endpoint, method, request body and headers`(
        apiVersion: PrimerApiVersion,
    ): Unit =
        runTest {
            val requestSlot = slot<Request>()
            val mockedOkHttpClient =
                mockk<OkHttpClient> {
                    coEvery { newCall(capture(requestSlot)) } returns mockk(relaxed = true)
                }
            val primerHttpClient =
                mockk<PrimerHttpClient> {
                    every { okHttpClient } returns mockedOkHttpClient
                    every { withTimeout(PRIMER_15S_TIMEOUT) } returns this
                }
            val tested = Remote3DSAuthDataSource(primerHttpClient) { apiVersion }
            val configuration =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "https://example.com"
                }
            val paymentMethodToken = "paymentMethodToken"
            val dataRequest =
                mockk<SuccessContinueAuthDataRequest>(relaxed = true) {
                    every { threeDsSdkProvider } returns ThreeDsSdkProvider.NETCETERA
                }

            val job = launch { tested.continue3dsAuth(configuration, paymentMethodToken, dataRequest) }
            delay(1.seconds)
            job.cancel()

            verify {
                mockedOkHttpClient.newCall(any())
            }
            val request = requestSlot.captured
            assertEquals("POST", request.method)
            assertEquals("https://example.com/3ds/paymentMethodToken/continue", request.url.toString())
            val expectedResult =
                Buffer().use { expectedSink ->
                    primerHttpClient.getRequestBody(dataRequest).writeTo(expectedSink)
                    expectedSink.readByteArray()
                }
            val actualResult =
                Buffer().use { actualSink ->
                    val body = requireNotNull(request.body) { "Request body cannot be null" }
                    body.writeTo(actualSink)
                    actualSink.readByteArray()
                }
            assertContentEquals(expectedResult, actualResult)
            assertEquals(apiVersion.toHeaderMap().toHeaders(), request.headers)
        }

    @Test
    fun `when continue3dsAuth is called, response is processed when the server responds in time`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_15S_TIMEOUT } returns 200.milliseconds
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse().setHeadersDelay(100, TimeUnit.MILLISECONDS))
                    start()
                }
            val configuration =
                mockk<ConfigurationData> {
                    every { pciUrl } returns mockWebServer.url("/").toString()
                }
            val paymentMethodToken = "paymentMethodToken"
            val dataRequest =
                mockk<SuccessContinueAuthDataRequest>(relaxed = true) {
                    every { threeDsSdkProvider } returns ThreeDsSdkProvider.NETCETERA
                }
            every { configuration.pciUrl } returns mockWebServer.url("/").toString()

            val tested =
                Remote3DSAuthDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                ) { PrimerApiVersion.LATEST }
            assertThrows<JsonDecodingException> {
                tested.continue3dsAuth(
                    configuration,
                    paymentMethodToken,
                    dataRequest,
                )
            }

            mockWebServer.shutdown()
            unmockkAll()
        }

    @Test
    fun `when continue3dsAuth is called, SocketTimeoutException is thrown when the server takes too long to respond`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_15S_TIMEOUT } returns 100.milliseconds
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse().setHeadersDelay(200, TimeUnit.MILLISECONDS))
                    start()
                }
            val configuration =
                mockk<ConfigurationData> {
                    every { pciUrl } returns mockWebServer.url("/").toString()
                }
            val paymentMethodToken = "paymentMethodToken"
            val dataRequest =
                mockk<SuccessContinueAuthDataRequest>(relaxed = true) {
                    every { threeDsSdkProvider } returns ThreeDsSdkProvider.NETCETERA
                }

            val tested =
                Remote3DSAuthDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                ) { PrimerApiVersion.LATEST }
            assertThrows<SocketTimeoutException> {
                tested.continue3dsAuth(
                    configuration,
                    paymentMethodToken,
                    dataRequest,
                )
            }

            mockWebServer.shutdown()
            unmockkAll()
        }
}
