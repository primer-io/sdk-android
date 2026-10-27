package io.primer.android.vault.implementation.vaultedMethods.data.datasource

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.transport.HttpTransport
import io.primer.android.core.data.network.utils.PrimerTimeouts
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.net.SocketTimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@ResourceLock("PrimerTimeouts")
class RemoteVaultedPaymentMethodsDataSourceTest {
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

    private val input =
        mockk<ConfigurationData> {
            every { pciUrl } returns "https://example.com"
        }

    @ParameterizedTest
    @EnumSource(value = PrimerApiVersion::class)
    fun `request is made with correct endpoint, method and headers`(apiVersion: PrimerApiVersion): Unit =
        runTest {
            val requestSlot = slot<Request>()
            val mockedOkHttpClient =
                mockk<OkHttpClient> {
                    coEvery { newCall(capture(requestSlot)) } returns mockk(relaxed = true)
                }
            val primerHttpClient =
                mockk<PrimerHttpClient> {
                    every { transport } returns HttpTransport(mockedOkHttpClient)
                    every { withTimeout(PRIMER_15S_TIMEOUT) } returns this
                }
            val tested = RemoteVaultedPaymentMethodsDataSource(primerHttpClient) { apiVersion }
            val job = launch { tested.execute(input) }
            delay(1.seconds)
            job.cancel()

            verify {
                mockedOkHttpClient.newCall(any())
            }
            val request = requestSlot.captured
            assertEquals("GET", request.method)
            assertEquals("https://example.com/payment-instruments", request.url.toString())
            apiVersion.toHeaderMap().forEach { (name, value) ->
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
            every { input.pciUrl } returns mockWebServer.url("/").toString()

            val tested =
                RemoteVaultedPaymentMethodsDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                ) { PrimerApiVersion.LATEST }
            assertThrows<JsonDecodingException> { tested.execute(input) }
        }

    @Test
    fun `SocketTimeoutException is thrown when the server takes too long to respond`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_15S_TIMEOUT } returns 100.milliseconds
            // The server never answers, so the timeout fires however slow the machine is.
            mockWebServer.apply {
                enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
                start()
            }
            every { input.pciUrl } returns mockWebServer.url("/").toString()

            val tested =
                RemoteVaultedPaymentMethodsDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                ) { PrimerApiVersion.LATEST }
            assertThrows<SocketTimeoutException> { tested.execute(input) }
        }
}
