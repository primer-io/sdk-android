package io.primer.android.vouchers.retailOutlets.implementation.rpc.data.datasource

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.utils.PrimerTimeouts
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models.RetailOutletDataRequest
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

class RemoteRetailOutletDataSourceTest {
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
            val input =
                mockk<BaseRemoteHostRequest<RetailOutletDataRequest>> {
                    every { host } returns mockWebServer.url("/").toString()
                    every { data } returns
                        RetailOutletDataRequest(
                            paymentMethodConfigId = "paymentMethodConfigId",
                        )
                }

            val tested =
                RemoteRetailOutletDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                )
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
            val input =
                mockk<BaseRemoteHostRequest<RetailOutletDataRequest>> {
                    every { host } returns mockWebServer.url("/").toString()
                    every { data } returns
                        RetailOutletDataRequest(
                            paymentMethodConfigId = "paymentMethodConfigId",
                        )
                }
            val tested =
                RemoteRetailOutletDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = mockk(),
                        messagePropertiesEventProvider = mockk(),
                    ),
                )
            assertThrows<SocketTimeoutException> { tested.execute(input) }

            mockWebServer.shutdown()
            unmockkAll()
        }
}
