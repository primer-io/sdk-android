package io.primer.jscore.infrastructure.core.datasource

import android.util.Base64
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.network.extensions.await
import io.primer.jscore.domain.core.models.JsResource
import io.primer.jscore.domain.core.models.JsResourceUrl
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.security.MessageDigest

internal class JsResourceDataSource(private val okHttpClient: OkHttpClient) :
    BaseSuspendDataSource<JsResource, JsResourceUrl> {
    override suspend fun execute(input: JsResourceUrl): JsResource {
        val (bytes, contentType) = fetchWithRetry(input.url)
        verifySha256(bytes, input.expectedSha256, input.url)

        return when (contentType) {
            WASM_CONTENT_TYPE.toMediaType() -> JsResource.Wasm(name = "wasm", bytes = bytes)
            else -> JsResource.Js(content = bytes.decodeToString())
        }
    }

    private suspend fun fetchWithRetry(url: String): FetchResult {
        lateinit var lastException: Exception
        repeat(MAX_RETRIES) { attempt ->
            try {
                val request = Request.Builder().url(url).get().build()
                val response: Response = okHttpClient.newCall(request).await()
                val responseBody = response.body ?: error("Missing response body.")
                if (response.isSuccessful.not()) {
                    throw Exception("Failed to download resource $url.")
                }
                return FetchResult(responseBody.bytes(), responseBody.contentType())
            } catch (e: Exception) {
                lastException = e
                if (attempt < MAX_RETRIES - 1) delay(RETRY_DELAY_MS)
            }
        }
        throw lastException
    }

    private data class FetchResult(val bytes: ByteArray, val contentType: okhttp3.MediaType?)

    private fun verifySha256(bytes: ByteArray, expectedSha256: String, url: String) {
        val digest = MessageDigest.getInstance("SHA-256")
        val actualSha256 = Base64.encodeToString(digest.digest(bytes), Base64.NO_WRAP)
        if (actualSha256 != expectedSha256) {
            throw SecurityException(
                "SHA-256 mismatch for resource $url. Expected: $expectedSha256, actual: $actualSha256",
            )
        }
    }

    private companion object {
        const val WASM_CONTENT_TYPE = "application/wasm"
        const val MAX_RETRIES = 3
        const val RETRY_DELAY_MS = 500L
    }
}
