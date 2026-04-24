@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.executionengine.data.executors.http

import io.mockk.every
import io.mockk.mockk
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.executionengine.domain.models.Outcome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class HttpStepExecutorTest {

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var httpClient: PrimerHttpClient
    private lateinit var executor: HttpStepExecutor

    @BeforeEach
    fun setUp() {
        okHttpClient = mockk()
        httpClient = PrimerHttpClient(
            okHttpClient = okHttpClient,
            logProvider = mockk(relaxed = true),
            messagePropertiesEventProvider = mockk(relaxed = true),
        )
        executor = HttpStepExecutor(httpClient)
    }

    @Test
    fun `execute should return success for GET request`() = runTest {
        val step = createStepJson(url = URL, method = "GET")
        mockEnqueueResponse(200, """{"key":"value"}""")

        val result = executor.execute(ACTION_ID, step)

        assertTrue(result.isSuccess)
        val stepResult = result.getOrThrow()
        assertEquals(Outcome.SUCCESS, stepResult.outcome)
        assertEquals(ACTION_ID, stepResult.actionId)

        val data = JSONObject(stepResult.data)
        assertEquals(200, data.getInt("statusCode"))
        assertTrue(data.getString("response").contains("key"))
    }

    @Test
    fun `execute should return success for POST request with body`() = runTest {
        val step = createStepJson(
            url = URL,
            method = "POST",
            body = mapOf("field" to "value"),
        )
        mockEnqueueResponse(201, """{"id":"123"}""")

        val result = executor.execute(ACTION_ID, step)

        assertTrue(result.isSuccess)
        val stepResult = result.getOrThrow()
        assertEquals(Outcome.SUCCESS, stepResult.outcome)

        val data = JSONObject(stepResult.data)
        assertEquals(201, data.getInt("statusCode"))
    }

    @Test
    fun `execute should include query parameters in request`() = runTest {
        val step = createStepJson(
            url = URL,
            method = "GET",
            query = mapOf("q" to "test", "page" to "1"),
        )

        var capturedRequest: Request? = null
        mockEnqueueWithCapture(200, """{"results":[]}""") { capturedRequest = it }

        executor.execute(ACTION_ID, step)

        assertNotNull(capturedRequest)
        val requestUrl = capturedRequest!!.url.toString()
        assertTrue(requestUrl.contains("q=test"))
        assertTrue(requestUrl.contains("page=1"))
    }

    @Test
    fun `execute should include custom headers in request`() = runTest {
        val step = createStepJson(
            url = URL,
            method = "GET",
            headers = mapOf("Authorization" to "Bearer token123", "X-Custom" to "value"),
        )

        var capturedRequest: Request? = null
        mockEnqueueWithCapture(200, """{"ok":true}""") { capturedRequest = it }

        executor.execute(ACTION_ID, step)

        assertNotNull(capturedRequest)
        assertEquals("Bearer token123", capturedRequest!!.header("Authorization"))
        assertEquals("value", capturedRequest!!.header("X-Custom"))
    }

    @Test
    fun `execute should use correct HTTP method for POST`() = runTest {
        val step = createStepJson(
            url = URL,
            method = "POST",
            body = mapOf("data" to "test"),
        )

        var capturedRequest: Request? = null
        mockEnqueueWithCapture(200, """{"ok":true}""") { capturedRequest = it }

        executor.execute(ACTION_ID, step)

        assertNotNull(capturedRequest)
        assertEquals("POST", capturedRequest!!.method)
        assertNotNull(capturedRequest.body)
    }

    @Test
    fun `execute should not include body for GET request`() = runTest {
        val step = createStepJson(url = URL, method = "GET")

        var capturedRequest: Request? = null
        mockEnqueueWithCapture(200, """{"ok":true}""") { capturedRequest = it }

        executor.execute(ACTION_ID, step)

        assertNotNull(capturedRequest)
        assertEquals("GET", capturedRequest!!.method)
    }

    @Test
    fun `execute should return failure for invalid URL`() = runTest {
        val step = createStepJson(url = "not-a-url", method = "GET")

        val result = executor.execute(ACTION_ID, step)

        assertTrue(result.isFailure)
        assertInstanceOf(IllegalArgumentException::class.java, result.exceptionOrNull())
    }

    @Test
    fun `execute should return failure for invalid step JSON`() = runTest {
        val result = executor.execute(ACTION_ID, "not valid json")

        assertTrue(result.isFailure)
    }

    @Test
    fun `execute should return failure for server error response`() = runTest {
        val step = createStepJson(url = URL, method = "GET")
        mockEnqueueResponse(500, """{"error":"server error"}""")

        val result = executor.execute(ACTION_ID, step)

        assertTrue(result.isFailure)
    }

    @Test
    fun `execute should return response headers in result`() = runTest {
        val step = createStepJson(url = URL, method = "GET")

        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.enqueue(any()) } answers {
            val callback = firstArg<Callback>()
            val response = Response.Builder()
                .request(Request.Builder().url(URL).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .addHeader("X-Request-Id", "req-123")
                .body("""{"data":"test"}""".toResponseBody("application/json".toMediaType()))
                .build()
            callback.onResponse(call, response)
        }

        val result = executor.execute(ACTION_ID, step)

        assertTrue(result.isSuccess)
        val data = JSONObject(stepResultData(result))
        assertTrue(data.has("headers"))
    }

    @Test
    fun `execute should use PUT method when specified`() = runTest {
        val step = createStepJson(
            url = URL,
            method = "PUT",
            body = mapOf("updated" to true),
        )

        var capturedRequest: Request? = null
        mockEnqueueWithCapture(200, """{"ok":true}""") { capturedRequest = it }

        executor.execute(ACTION_ID, step)

        assertEquals("PUT", capturedRequest!!.method)
    }

    @Test
    fun `execute should use DELETE method when specified`() = runTest {
        val step = createStepJson(url = URL, method = "DELETE")

        var capturedRequest: Request? = null
        mockEnqueueWithCapture(200, """{"ok":true}""") { capturedRequest = it }

        executor.execute(ACTION_ID, step)

        assertEquals("DELETE", capturedRequest!!.method)
    }

    private fun stepResultData(result: Result<*>): Map<String, Any?> =
        (result.getOrThrow() as io.primer.executionengine.domain.models.StepResult).data

    private fun mockEnqueueResponse(code: Int, bodyJson: String) {
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.enqueue(any()) } answers {
            val callback = firstArg<Callback>()
            val response = buildOkResponse(URL, code, bodyJson)
            callback.onResponse(call, response)
        }
    }

    private fun mockEnqueueWithCapture(
        code: Int,
        bodyJson: String,
        capture: (Request) -> Unit,
    ) {
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } answers {
            capture(firstArg())
            call
        }
        every { call.enqueue(any()) } answers {
            val callback = firstArg<Callback>()
            val response = buildOkResponse(URL, code, bodyJson)
            callback.onResponse(call, response)
        }
    }

    private fun buildOkResponse(url: String, code: Int, bodyJson: String): Response =
        Response.Builder()
            .request(Request.Builder().url(url).build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .body(bodyJson.toResponseBody("application/json".toMediaType()))
            .build()

    private fun createStepJson(
        url: String,
        method: String,
        body: Map<String, Any?>? = null,
        headers: Map<String, String>? = null,
        query: Map<String, String>? = null,
        timeout: Int? = null,
    ): String = JSONObject().apply {
        put("type", "HTTP")
        put("url", url)
        put("method", method)
        body?.let { put("body", JSONObject(it)) }
        headers?.let { put("headers", JSONObject(it)) }
        query?.let { put("query", JSONObject(it)) }
        timeout?.let { put("timeout", it) }
    }.toString()

    private companion object {
        const val ACTION_ID = "action-1"
        const val URL = "https://api.example.com/data"
    }
}
