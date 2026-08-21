package io.primer.android.payments.core.status.data.datasource

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.exception.JsonDecodingException
import io.primer.android.core.data.network.helpers.MessageLog
import io.primer.android.core.data.network.helpers.MessagePropertiesHelper
import io.primer.android.core.data.network.helpers.MessageTypeHelper
import io.primer.android.core.data.network.utils.PrimerTimeouts
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT
import io.primer.android.core.utils.EventFlowProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class RemoteAsyncPaymentMethodStatusDataSourceTest {

    @AfterEach
    fun tearDown() {
        PrimerHttpClient.clearCustomTimeoutInstances()
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
            val input = mockWebServer.url("/").toString()

            val tested =
                RemoteAsyncPaymentMethodStatusDataSource(
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
    fun `IOException is thrown when all the retries are exhausted due to network conditions`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_60S_TIMEOUT } returns 1000.milliseconds
            val mockWebServer =
                MockWebServer().apply {
                    enqueue(MockResponse().setHeadersDelay(1000, TimeUnit.MILLISECONDS))
                    start()
                }
            val messagePropsFlow =
                MutableStateFlow<MessagePropertiesHelper?>(null)

            val messagePropertiesEventProvider =
                mockk<EventFlowProvider<MessagePropertiesHelper>>()

            val logFlow =
                MutableStateFlow<MessageLog?>(
                    value = null,
                )

            val logProvider =
                mockk<EventFlowProvider<MessageLog>>()

            every {
                logProvider.getEventProvider()
            } returns logFlow

            every {
                messagePropertiesEventProvider.getEventProvider()
            } returns messagePropsFlow
            val input = mockWebServer.url("/").toString()
            val tested =
                RemoteAsyncPaymentMethodStatusDataSource(
                    PrimerHttpClient(
                        okHttpClient = OkHttpClient().newBuilder().build(),
                        logProvider = logProvider,
                        messagePropertiesEventProvider = messagePropertiesEventProvider,
                    ),
                )
            assertThrows<IOException> { tested.execute(input) }
            assertEquals(
                MessageTypeHelper.RETRY_FAILED,
                messagePropsFlow.value?.messageTypeHelper,
            )

            mockWebServer.shutdown()
            unmockkAll()
        }
}
