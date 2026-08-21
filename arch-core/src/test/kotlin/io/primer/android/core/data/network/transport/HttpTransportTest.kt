package io.primer.android.core.data.network.transport

import io.primer.android.core.data.network.exception.HttpRequestTimeoutException
import io.primer.android.core.data.network.retry.HttpRetryLoop
import io.primer.android.core.data.network.retry.HttpTransportResult
import io.primer.android.core.data.network.retry.RetryAttempt
import io.primer.android.core.data.network.retry.RetryAttemptError
import io.primer.android.core.data.network.retry.RetryEventListener
import io.primer.android.core.data.network.retry.RetryPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HttpTransportTest {
    private class RecordingRetryListener : RetryEventListener {
        val scheduled = mutableListOf<RetryAttempt>()

        override suspend fun onRetryScheduled(attempt: RetryAttempt, maxAttempts: Int) {
            scheduled += attempt
        }
    }

    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer().apply { start() }
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun getRequest(): Request =
        Request.Builder()
            .url(mockWebServer.url("/").toString())
            .get()
            .build()

    @Test
    fun `the same X-Request-Id is stamped on every retry attempt`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setResponseCode(500))
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
            val exchange = HttpTransport(OkHttpClient())

            val result =
                exchange.execute(
                    request = getRequest(),
                    retryPolicy = RetryPolicy(maxAttempts = 2, baseDelayMs = 1L, retryOn = listOf(500)),
                )

            val firstRequestId = mockWebServer.takeRequest().getHeader("X-Request-Id")
            val secondRequestId = mockWebServer.takeRequest().getHeader("X-Request-Id")
            assertNotNull(firstRequestId)
            assertEquals(firstRequestId, secondRequestId)
            val settled = assertIs<HttpTransportResult.Settled>(result)
            assertEquals(200, settled.response.statusCode)
            assertEquals(firstRequestId, settled.response.requestId)
            assertEquals(2, mockWebServer.requestCount)
        }

    @Test
    fun `a per-attempt timeout produces a stamped transport timeout which retries under a policy`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
            val listener = RecordingRetryListener()
            val exchange = HttpTransport(OkHttpClient(), HttpRetryLoop(listener = listener))

            val result =
                withContext(Dispatchers.IO) {
                    exchange.execute(
                        request = getRequest(),
                        timeoutMs = 500L,
                        retryPolicy = RetryPolicy(maxAttempts = 2, baseDelayMs = 1L),
                    )
                }

            val settled = assertIs<HttpTransportResult.Settled>(result)
            assertEquals(200, settled.response.statusCode)
            assertEquals(2, mockWebServer.requestCount)
            val attemptError = assertIs<RetryAttemptError.Thrown>(listener.scheduled.single().error)
            val timeoutException = assertIs<HttpRequestTimeoutException>(attemptError.cause)
            assertEquals(500L, timeoutException.timeoutMs)
        }

    @Test
    fun `a null policy and null strategy make exactly one request and return the settled non-2xx`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("oops"))
            val exchange = HttpTransport(OkHttpClient())

            val result = exchange.execute(request = getRequest())

            val settled = assertIs<HttpTransportResult.Settled>(result)
            assertEquals(500, settled.response.statusCode)
            assertEquals("oops", settled.response.bodyText)
            assertEquals(1, mockWebServer.requestCount)
        }

    @Test
    fun `a bodyless POST sends an empty body instead of failing`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
            val exchange = HttpTransport(OkHttpClient())

            val result =
                exchange.execute(
                    request = RawHttpRequest(
                        method = RawHttpMethod.POST,
                        url = mockWebServer.url("/").toString(),
                        requestId = "request-id",
                    ),
                )

            val settled = assertIs<HttpTransportResult.Settled>(result)
            assertEquals(200, settled.response.statusCode)
            val recorded = mockWebServer.takeRequest()
            assertEquals("POST", recorded.method)
            assertEquals(0L, recorded.bodySize)
        }

    @Test
    fun `an enclosing timeout propagates as cancellation instead of being stamped`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val exchange = HttpTransport(OkHttpClient())

            assertFailsWith<TimeoutCancellationException> {
                withTimeout(200L) {
                    exchange.execute(request = getRequest())
                }
            }
        }

    @Test
    fun `caller coroutine cancellation propagates`() =
        runTest {
            mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val exchange = HttpTransport(OkHttpClient())
            var completed = false

            val job =
                launch {
                    exchange.execute(request = getRequest())
                    completed = true
                }
            runCurrent()
            // Ensure the request reached the server before cancelling the caller.
            assertNotNull(mockWebServer.takeRequest())

            job.cancel()
            job.join()

            assertTrue(job.isCancelled)
            assertFalse(completed)
        }
}
