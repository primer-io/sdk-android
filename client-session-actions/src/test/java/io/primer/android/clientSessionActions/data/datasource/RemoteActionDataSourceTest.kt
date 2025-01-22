@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.clientSessionActions.data.datasource

import io.mockk.coEvery
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.primer.android.clientSessionActions.data.models.ClientSessionActionsDataRequest
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.utils.PrimerTimeouts
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@ExtendWith(MockKExtension::class)
internal class RemoteActionDataSourceTest {
    private val dataRequest = ClientSessionActionsDataRequest(actions = emptyList())

    private val input =
        mockk<BaseRemoteHostRequest<ClientSessionActionsDataRequest>> {
            every { host } returns "https://example.com"
            every { data } returns dataRequest
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
                    every { okHttpClient } returns mockedOkHttpClient
                    every { withTimeout(PRIMER_15S_TIMEOUT) } returns this
                }
            val tested = RemoteActionDataSource(primerHttpClient) { apiVersion }

            val job = launch { tested.execute(input) }
            delay(1.seconds)
            job.cancel()

            verify {
                mockedOkHttpClient.newCall(any())
            }
            val request = requestSlot.captured
            assertEquals("POST", request.method)
            assertEquals("https://example.com/client-session/actions", request.url.toString())
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
            every { PRIMER_15S_TIMEOUT } returns 200.milliseconds
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse().setHeadersDelay(100, TimeUnit.MILLISECONDS))
                    start()
                }
            every { input.host } returns mockWebServer.url("/").toString()

            val tested =
                RemoteActionDataSource(
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
            every { PRIMER_15S_TIMEOUT } returns 100.milliseconds
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse().setHeadersDelay(200, TimeUnit.MILLISECONDS))
                    start()
                }
            every { input.host } returns mockWebServer.url("/").toString()

            val tested =
                RemoteActionDataSource(
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
