package io.primer.android.payments.core.status.data.datasource

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
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
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class RemoteAsyncPaymentMethodStatusDataSourceTest {
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
        }

    @Test
    fun `IOException is thrown when all the retries are exhausted due to network conditions`() =
        runTest {
            mockkObject(PrimerTimeouts)
            every { PRIMER_60S_TIMEOUT } returns 100.milliseconds
            // The server never answers, so the timeout fires however slow the machine is.
            mockWebServer.apply {
                enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
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
        }
}
