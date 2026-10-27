package io.primer.android.payments.core.create.data.datasource

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderPair
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.transport.HttpTransport
import io.primer.android.core.data.network.utils.PrimerTimeouts
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.payments.core.create.data.model.CreatePaymentDataRequest
import io.primer.android.payments.core.idempotency.IdempotencyKeyHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.net.SocketTimeoutException
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class CreatePaymentDataSourceTest {
    private val mockWebServer = MockWebServer()

    @BeforeEach
    fun setUp() {
        PrimerHttpClient.clearCustomTimeoutInstances()
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
        unmockkObject(PrimerTimeouts)
        PrimerHttpClient.clearCustomTimeoutInstances()
    }

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
                    every { transport } returns HttpTransport(mockedOkHttpClient)
                    every { withTimeout(PRIMER_60S_TIMEOUT) } returns this
                }
            val idempotencyKeyHolder = mockk<IdempotencyKeyHolder> {
                every { get() } returns null
            }
            val tested = CreatePaymentDataSource(primerHttpClient, { apiVersion }, idempotencyKeyHolder)
            val dataRequest = CreatePaymentDataRequest("paymentMethodToken")
            val input =
                mockk<BaseRemoteHostRequest<CreatePaymentDataRequest>> {
                    every { host } returns "https://example.com"
                    every { data } returns dataRequest
                }

            val job = launch { tested.execute(input) }
            delay(1.seconds)
            job.cancel()

            verify {
                mockedOkHttpClient.newCall(any())
            }
            val request = requestSlot.captured
            assertEquals("POST", request.method)
            assertEquals("https://example.com/payments", request.url.toString())
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
            mapOf(
                apiVersion.toHeaderPair(),
                "accept" to "*/*",
            ).forEach { (name, value) ->
                assertEquals(value, request.header(name))
            }
            assertNotNull(request.header("X-Request-Id"))
        }

    @Test
    fun `response is processed when the server responds in time`() =
        runTest {
            mockWebServer.apply {
                enqueue(MockResponse())
                start()
            }
            val dataRequest = CreatePaymentDataRequest(paymentMethodToken = "paymentMethodToken")
            val input =
                mockk<BaseRemoteHostRequest<CreatePaymentDataRequest>> {
                    every { host } returns mockWebServer.url("/").toString()
                    every { data } returns dataRequest
                }

            val idempotencyKeyHolder = mockk<IdempotencyKeyHolder> {
                every { get() } returns null
            }
            val tested =
                CreatePaymentDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                    { PrimerApiVersion.LATEST },
                    idempotencyKeyHolder,
                )
            assertThrows<JsonDecodingException> { tested.execute(input) }
        }

    @Test
    fun `SocketTimeoutException is thrown when the server takes too long to respond`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_60S_TIMEOUT } returns 100.milliseconds
            // The server never answers, so the timeout fires however slow the machine is.
            mockWebServer.apply {
                enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
                start()
            }
            val dataRequest = CreatePaymentDataRequest(paymentMethodToken = "paymentMethodToken")
            val input =
                mockk<BaseRemoteHostRequest<CreatePaymentDataRequest>> {
                    every { host } returns mockWebServer.url("/").toString()
                    every { data } returns dataRequest
                }

            val idempotencyKeyHolder = mockk<IdempotencyKeyHolder> {
                every { get() } returns null
            }
            val tested =
                CreatePaymentDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                    { PrimerApiVersion.LATEST },
                    idempotencyKeyHolder,
                )
            assertThrows<SocketTimeoutException> { tested.execute(input) }
        }
}
