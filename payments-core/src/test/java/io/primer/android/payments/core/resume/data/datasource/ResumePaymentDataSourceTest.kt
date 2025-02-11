package io.primer.android.payments.core.resume.data.datasource

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.utils.PrimerTimeouts
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.payments.core.resume.data.model.ResumePaymentDataRequest
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

class ResumePaymentDataSourceTest {
    @ParameterizedTest
    @EnumSource(value = PrimerApiVersion::class)
    fun `request is made with correct endpoint, method, request body and headers`(apiVersion: PrimerApiVersion): Unit =
        runTest {
            val requestSlot = slot<Request>()
            val mockedOkHttpClient =
                mockk<OkHttpClient> {
                    coEvery { newCall(capture(requestSlot)) } returns mockk(relaxed = true)
                }
            val primerHttpClient =
                mockk<PrimerHttpClient> {
                    every { okHttpClient } returns mockedOkHttpClient
                    every { withTimeout(PRIMER_60S_TIMEOUT) } returns this
                }
            val tested = ResumePaymentDataSource(primerHttpClient) { apiVersion }
            val dataRequest = ResumePaymentDataRequest(resumeToken = "resumeToken")
            val input =
                mockk<BaseRemoteHostRequest<Pair<String, ResumePaymentDataRequest>>> {
                    every { host } returns "https://example.com"
                    every { data } returns Pair("paymentId", dataRequest)
                }

            val job = launch { tested.execute(input) }
            delay(1.seconds)
            job.cancel()

            verify {
                mockedOkHttpClient.newCall(any())
            }
            val request = requestSlot.captured
            assertEquals("POST", request.method)
            assertEquals("https://example.com/payments/paymentId/resume", request.url.toString())
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
    fun `response is processed when the server responds in time`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_60S_TIMEOUT } returns 200.milliseconds
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse().setHeadersDelay(100, TimeUnit.MILLISECONDS))
                    start()
                }
            val dataRequest = ResumePaymentDataRequest(resumeToken = "resumeToken")
            val input =
                mockk<BaseRemoteHostRequest<Pair<String, ResumePaymentDataRequest>>> {
                    every { host } returns mockWebServer.url("/").toString()
                    every { data } returns Pair("paymentId", dataRequest)
                }

            val tested =
                ResumePaymentDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                ) { PrimerApiVersion.LATEST }
            assertThrows<JsonDecodingException> { tested.execute(input) }

            mockWebServer.shutdown()
            unmockkAll()
        }

    @Test
    fun `SocketTimeoutException is thrown when the server takes too long to respond`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_60S_TIMEOUT } returns 100.milliseconds
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse().setHeadersDelay(200, TimeUnit.MILLISECONDS))
                    start()
                }
            val dataRequest = ResumePaymentDataRequest(resumeToken = "resumeToken")
            val input =
                mockk<BaseRemoteHostRequest<Pair<String, ResumePaymentDataRequest>>> {
                    every { host } returns mockWebServer.url("/").toString()
                    every { data } returns Pair("paymentId", dataRequest)
                }

            val tested =
                ResumePaymentDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                ) { PrimerApiVersion.LATEST }
            assertThrows<SocketTimeoutException> { tested.execute(input) }

            mockWebServer.shutdown()
            unmockkAll()
        }
}
