package io.primer.executionengine.data.executors.http

import io.mockk.mockk
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.executionengine.domain.models.Outcome
import io.primer.executionengine.domain.models.StepResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class HttpRequestStepExecutorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var executor: HttpRequestStepExecutor

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer().apply { start() }
        val httpClient = PrimerHttpClient(
            okHttpClient = OkHttpClient(),
            logProvider = mockk(relaxed = true),
            messagePropertiesEventProvider = mockk(relaxed = true),
        )
        executor = HttpRequestStepExecutor(
            httpClient = httpClient,
            requestIdProvider = { REQUEST_ID },
        )
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `execute rejects an unknown method with a message echoing the received value`() = runTest {
        val result = executor.execute(ACTION_ID, stepJson(method = "TRACE"))

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertInstanceOf(IllegalArgumentException::class.java, exception)
        assertTrue(exception!!.message!!.contains("TRACE"))
        assertTrue(exception.message!!.contains("GET"))
        assertEquals(0, mockWebServer.requestCount)
    }

    @Test
    fun `execute rejects a GET request with a body`() = runTest {
        val result = executor.execute(ACTION_ID, stepJson(method = "GET", body = JSONObject().put("a", 1)))

        assertTrue(result.isFailure)
        assertInstanceOf(IllegalArgumentException::class.java, result.exceptionOrNull())
        assertEquals(0, mockWebServer.requestCount)
    }

    @Test
    fun `execute allows a DELETE request with a body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val result = executeOnIo(stepJson(method = "DELETE", body = JSONObject().put("a", 1)))

        assertEquals(Outcome.SUCCESS, result.getOrThrow().outcome)
        val recorded = mockWebServer.takeRequest()
        assertEquals("DELETE", recorded.method)
        assertEquals("""{"a":1}""", recorded.body.readUtf8())
    }

    @Test
    fun `execute sends the method url and serialized JSON body`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        executeOnIo(stepJson(method = "POST", body = JSONObject().put("field", "value")))

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/checkout", recorded.path)
        assertEquals("""{"field":"value"}""", recorded.body.readUtf8())
        assertTrue(recorded.getHeader("Content-Type")!!.startsWith("application/json"))
    }

    @Test
    fun `execute omits the body and Content-Type header when body is absent`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        executeOnIo(stepJson(method = "GET"))

        val recorded = mockWebServer.takeRequest()
        assertEquals("GET", recorded.method)
        assertNull(recorded.getHeader("Content-Type"))
        assertEquals(0L, recorded.bodySize)
    }

    @Test
    fun `execute sends a bodyless POST with an empty body and no Content-Type header`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val result = executeOnIo(stepJson(method = "POST"))

        assertTrue(result.isSuccess)
        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertNull(recorded.getHeader("Content-Type"))
        assertEquals(0L, recorded.bodySize)
    }

    @Test
    fun `execute sends the idempotency key verbatim when provided`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        executeOnIo(stepJson(method = "GET", idempotencyKey = "key-123"))

        assertEquals("key-123", mockWebServer.takeRequest().getHeader("X-Idempotency-Key"))
    }

    @Test
    fun `execute never generates an idempotency key when none is provided`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        executeOnIo(stepJson(method = "GET"))

        assertNull(mockWebServer.takeRequest().getHeader("X-Idempotency-Key"))
    }

    @Test
    fun `execute stamps the same X-Request-Id on both attempts of a retried request`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{}"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        val retry = JSONObject().put("maxAttempts", 2).put("retryOn", JSONArray(listOf(500))).put("baseDelayMs", 1L)

        val result = executeOnIo(stepJson(method = "GET", retry = retry))

        assertEquals(Outcome.SUCCESS, result.getOrThrow().outcome)
        assertEquals(2, mockWebServer.requestCount)
        assertEquals(REQUEST_ID, mockWebServer.takeRequest().getHeader("X-Request-Id"))
        assertEquals(REQUEST_ID, mockWebServer.takeRequest().getHeader("X-Request-Id"))
    }

    @Test
    fun `execute makes exactly one attempt on a 500 when no retry field is present`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("""{"error":"server error"}"""))

        val result = executeOnIo(stepJson(method = "GET"))

        assertEquals(1, mockWebServer.requestCount)
        val stepResult = result.getOrThrow()
        assertEquals(Outcome.ERROR, stepResult.outcome)
        assertEquals(500, stepResult.data["status"])
        assertEquals(false, stepResult.data["success"])
        assertEquals(mapOf("error" to "server error"), stepResult.data["body"])
    }

    @Test
    fun `execute does not retry a 500 when retry is present without retryOn`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{}"))
        val retry = JSONObject().put("maxAttempts", 3).put("baseDelayMs", 1L)

        val result = executeOnIo(stepJson(method = "GET", retry = retry))

        assertEquals(1, mockWebServer.requestCount)
        assertEquals(Outcome.ERROR, result.getOrThrow().outcome)
    }

    @Test
    fun `execute retries up to maxAttempts when retryOn matches the status`() = runTest {
        repeat(3) { mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{}")) }
        val retry = JSONObject().put("maxAttempts", 3).put("retryOn", JSONArray(listOf(500))).put("baseDelayMs", 1L)

        val result = executeOnIo(stepJson(method = "GET", retry = retry))

        assertEquals(3, mockWebServer.requestCount)
        assertEquals(Outcome.ERROR, result.getOrThrow().outcome)
    }

    @Test
    fun `execute returns SUCCESS with the parsed body map for a 2xx response`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"key":"value"}"""))

        val result = executeOnIo(stepJson(method = "GET"))

        val stepResult = result.getOrThrow()
        assertEquals(Outcome.SUCCESS, stepResult.outcome)
        assertEquals(ACTION_ID, stepResult.actionId)
        assertEquals(200, stepResult.data["status"])
        assertEquals(true, stepResult.data["success"])
        assertEquals(mapOf("key" to "value"), stepResult.data["body"])
    }

    @Test
    fun `execute returns ERROR with readable status and body for a non-2xx response`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"not found"}"""))

        val result = executeOnIo(stepJson(method = "GET"))

        val stepResult = result.getOrThrow()
        assertEquals(Outcome.ERROR, stepResult.outcome)
        assertEquals(404, stepResult.data["status"])
        assertEquals(false, stepResult.data["success"])
        assertEquals(mapOf("message" to "not found"), stepResult.data["body"])
    }

    @Test
    fun `execute returns the raw text when the response body is not JSON`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("plain text"))

        val result = executeOnIo(stepJson(method = "GET"))

        assertEquals("plain text", result.getOrThrow().data["body"])
    }

    @Test
    fun `execute returns a null body when the response body is empty`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        val result = executeOnIo(stepJson(method = "GET"))

        val stepResult = result.getOrThrow()
        assertEquals(Outcome.SUCCESS, stepResult.outcome)
        assertNull(stepResult.data["body"])
    }

    @Test
    fun `execute returns single-valued headers taking the last value of a multimap entry`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader("X-Multi", "one")
                .addHeader("X-Multi", "two"),
        )

        val result = executeOnIo(stepJson(method = "GET"))

        val headers = result.getOrThrow().data["headers"] as Map<*, *>
        assertEquals("two", headers["x-multi"])
    }

    @Test
    fun `execute returns failure when the server never responds and no retry is configured`() = runTest {
        val deadServer = MockWebServer().apply { start() }
        val deadUrl = deadServer.url("/dead").toString()
        deadServer.shutdown()

        val result = executeOnIo(stepJson(method = "GET", url = deadUrl))

        assertTrue(result.isFailure)
    }

    @Test
    fun `onFinish aborts an in-flight request and the executor works again afterwards`() = runTest {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val step = stepJson(method = "GET")

        val deferred = async(Dispatchers.IO) { executor.execute(ACTION_ID, step) }
        mockWebServer.takeRequest()
        executor.onFinish()
        val aborted = deferred.await()

        assertTrue(aborted.isFailure)
        assertInstanceOf(HttpStepAbortedException::class.java, aborted.exceptionOrNull())
        assertEquals("http.request aborted", aborted.exceptionOrNull()!!.message)

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))
        val second = executeOnIo(step)
        assertEquals(Outcome.SUCCESS, second.getOrThrow().outcome)
        assertEquals(mapOf("ok" to true), second.getOrThrow().data["body"])
    }

    private suspend fun executeOnIo(step: String): Result<StepResult> =
        withContext(Dispatchers.IO) { executor.execute(ACTION_ID, step) }

    private fun stepJson(
        method: String,
        url: String = mockWebServer.url("/checkout").toString(),
        body: Any? = null,
        retry: JSONObject? = null,
        idempotencyKey: String? = null,
    ): String = JSONObject().apply {
        put("method", method)
        put("url", url)
        body?.let { put("body", it) }
        retry?.let { put("retry", it) }
        idempotencyKey?.let { put("idempotencyKey", it) }
    }.toString()

    private companion object {
        const val ACTION_ID = "action-1"
        const val REQUEST_ID = "test-request-id"
    }
}
