package io.primer.android.configuration.data.datasource

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.utils.PrimerTimeouts
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RemoteConfigurationDataSourceTest {
    @ParameterizedTest
    @EnumSource(value = PrimerApiVersion::class)
    fun `request is made with correct endpoint, method and headers`(apiVersion: PrimerApiVersion): Unit =
        runTest {
            val url = "https://example.com"
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
            val tested = RemoteConfigurationDataSource(primerHttpClient) { apiVersion }

            val job = launch { tested.execute(url) }
            delay(1.seconds)
            job.cancel()

            verify {
                mockedOkHttpClient.newCall(any())
            }
            val request = requestSlot.captured
            assertEquals("GET", request.method)
            assertEquals("https://example.com/", request.url.toString())
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
            val input = mockWebServer.url("/").toString()
            val tested =
                RemoteConfigurationDataSource(
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
    fun `IOException is thrown when the server takes too long to respond`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_15S_TIMEOUT } returns 100.milliseconds
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse().setHeadersDelay(200, TimeUnit.MILLISECONDS))
                    start()
                }
            val input = mockWebServer.url("/").toString()
            val tested =
                RemoteConfigurationDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(relaxed = true),
                        messagePropertiesEventProvider = mockk(relaxed = true),
                    ),
                ) { PrimerApiVersion.LATEST }
            assertThrows<IOException> { tested.execute(input) }
            mockWebServer.shutdown()
            unmockkAll()
        }
}
