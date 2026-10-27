package io.primer.android.klarna.implementation.session.data.datasource

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.utils.PrimerTimeouts
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.klarna.implementation.session.data.models.FinalizeKlarnaSessionDataRequest
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.ResourceLock
import java.net.SocketTimeoutException
import kotlin.time.Duration.Companion.milliseconds

@ResourceLock("PrimerTimeouts")
class RemoteFinalizeKlarnaSessionDataSourceTest {
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

    @Test
    fun `response is processed when the server responds in time`() =
        runTest {
            mockWebServer.apply {
                enqueue(MockResponse())
                start()
            }
            val input =
                mockk<BaseRemoteHostRequest<FinalizeKlarnaSessionDataRequest>> {
                    every { host } returns mockWebServer.url("/").toString()
                    every { data } returns
                        FinalizeKlarnaSessionDataRequest(
                            paymentMethodConfigId = "paymentMethodConfigId",
                            sessionId = "sessionId",
                        )
                }

            val tested =
                RemoteFinalizeKlarnaSessionDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
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
            val input =
                mockk<BaseRemoteHostRequest<FinalizeKlarnaSessionDataRequest>> {
                    every { host } returns mockWebServer.url("/").toString()
                    every { data } returns
                        FinalizeKlarnaSessionDataRequest(
                            paymentMethodConfigId = "paymentMethodConfigId",
                            sessionId = "sessionId",
                        )
                }
            val tested =
                RemoteFinalizeKlarnaSessionDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                )
            assertThrows<SocketTimeoutException> { tested.execute(input) }
        }
}
