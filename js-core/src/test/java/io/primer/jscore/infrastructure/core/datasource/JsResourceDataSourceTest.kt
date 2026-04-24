@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.jscore.infrastructure.core.datasource

import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.primer.jscore.domain.core.models.JsResource
import io.primer.jscore.domain.core.models.JsResourceUrl
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.security.MessageDigest

internal class JsResourceDataSourceTest {

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var dataSource: JsResourceDataSource

    @BeforeEach
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg())
        }

        okHttpClient = mockk()
        dataSource = JsResourceDataSource(okHttpClient)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Base64::class)
    }

    @Test
    fun `execute should return Js resource for non-wasm content type`() = runTest {
        val jsContent = "console.log('hello')"
        val bytes = jsContent.toByteArray()
        val sha256 = computeSha256Base64(bytes)
        val input = JsResourceUrl(url = "https://example.com/script.js", expectedSha256 = sha256)

        mockEnqueueSuccess(bytes, "application/javascript")

        val result = dataSource.execute(input)

        assertInstanceOf(JsResource.Js::class.java, result)
        assertEquals(jsContent, (result as JsResource.Js).content)
    }

    @Test
    fun `execute should return Wasm resource for wasm content type`() = runTest {
        val wasmBytes = byteArrayOf(0x00, 0x61, 0x73, 0x6D)
        val sha256 = computeSha256Base64(wasmBytes)
        val input = JsResourceUrl(url = "https://example.com/module.wasm", expectedSha256 = sha256)

        mockEnqueueSuccess(wasmBytes, "application/wasm")

        val result = dataSource.execute(input)

        assertInstanceOf(JsResource.Wasm::class.java, result)
        assertEquals(wasmBytes.toList(), (result as JsResource.Wasm).bytes.toList())
    }

    @Test
    fun `execute should throw SecurityException when sha256 does not match`() {
        assertThrows<SecurityException> {
            runTest {
                val bytes = "content".toByteArray()
                val input = JsResourceUrl(
                    url = "https://example.com/script.js",
                    expectedSha256 = "wrong-hash",
                )

                mockEnqueueSuccess(bytes, "application/javascript")

                dataSource.execute(input)
            }
        }
    }

    @Test
    fun `execute should throw when response is not successful`() {
        assertThrows<Exception> {
            runTest {
                val input = JsResourceUrl(
                    url = "https://example.com/script.js",
                    expectedSha256 = "any",
                )

                mockEnqueueResponse(code = 500, bytes = "error".toByteArray(), contentType = "text/plain")

                dataSource.execute(input)
            }
        }
    }

    @Test
    fun `execute should retry on IOException and succeed`() = runTest {
        val jsContent = "var x = 1"
        val bytes = jsContent.toByteArray()
        val sha256 = computeSha256Base64(bytes)
        val input = JsResourceUrl(url = "https://example.com/script.js", expectedSha256 = sha256)

        var callCount = 0
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.enqueue(any()) } answers {
            val callback = firstArg<Callback>()
            callCount++
            if (callCount == 1) {
                callback.onFailure(call, IOException("network error"))
            } else {
                val response = buildResponse(input.url, 200, bytes, "application/javascript")
                callback.onResponse(call, response)
            }
        }

        val result = dataSource.execute(input)

        assertInstanceOf(JsResource.Js::class.java, result)
        assertEquals(jsContent, (result as JsResource.Js).content)
        assertEquals(2, callCount)
    }

    private fun mockEnqueueSuccess(bytes: ByteArray, contentType: String) {
        mockEnqueueResponse(code = 200, bytes = bytes, contentType = contentType)
    }

    private fun mockEnqueueResponse(code: Int, bytes: ByteArray, contentType: String) {
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.enqueue(any()) } answers {
            val callback = firstArg<Callback>()
            val response = buildResponse("https://example.com", code, bytes, contentType)
            callback.onResponse(call, response)
        }
    }

    private fun buildResponse(
        url: String,
        code: Int,
        bytes: ByteArray,
        contentType: String,
    ): Response = Response.Builder()
        .request(Request.Builder().url(url).build())
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(if (code == 200) "OK" else "Error")
        .body(bytes.toResponseBody(contentType.toMediaType()))
        .build()

    private fun computeSha256Base64(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return java.util.Base64.getEncoder().encodeToString(digest.digest(bytes))
    }
}
