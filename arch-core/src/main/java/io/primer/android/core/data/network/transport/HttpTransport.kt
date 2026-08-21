package io.primer.android.core.data.network.transport

import io.primer.android.core.data.network.CONTENT_TYPE_APPLICATION_JSON
import io.primer.android.core.data.network.exception.HttpRequestTimeoutException
import io.primer.android.core.data.network.extensions.await
import io.primer.android.core.data.network.retry.HttpAttemptResult
import io.primer.android.core.data.network.retry.HttpRetryLoop
import io.primer.android.core.data.network.retry.HttpTransportResult
import io.primer.android.core.data.network.retry.RetryPolicy
import io.primer.android.core.data.network.retry.RetryStrategy
import io.primer.android.core.data.network.retry.resolve
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

/**
 * Raw (non-throwing) HTTP execution path: runs a request (optionally through the retry loop) and
 * returns an [HttpTransportResult] instead of throwing on transport errors or non-2xx statuses.
 * Cancellation is the only thing that propagates.
 */
class HttpTransport(
    val okHttpClient: OkHttpClient,
    private val retryLoop: HttpRetryLoop = HttpRetryLoop(),
) {
    suspend fun execute(
        request: RawHttpRequest,
        retryPolicy: RetryPolicy? = null,
        retryStrategy: RetryStrategy? = null,
    ): HttpTransportResult =
        execute(
            request = request.toOkHttpRequest(),
            timeoutMs = request.timeoutMs,
            retryPolicy = retryPolicy,
            retryStrategy = retryStrategy,
            requestId = request.requestId,
        )

    /**
     * Executes [request], stamping `X-Request-Id` = [requestId] on every attempt. When both
     * [retryPolicy] and [retryStrategy] are `null`, exactly one attempt is made (no loop, no
     * defaults).
     */
    suspend fun execute(
        request: Request,
        timeoutMs: Long? = null,
        retryPolicy: RetryPolicy? = null,
        retryStrategy: RetryStrategy? = null,
        requestId: String = UUID.randomUUID().toString(),
    ): HttpTransportResult {
        val stampedRequest =
            request.newBuilder()
                .header(HEADER_REQUEST_ID, requestId)
                .build()
        return if (retryPolicy == null && retryStrategy == null) {
            when (val result = runAttempt(stampedRequest, timeoutMs, requestId)) {
                is HttpAttemptResult.Response -> HttpTransportResult.Settled(result.response)
                is HttpAttemptResult.TransportError -> HttpTransportResult.Failed(result.error)
            }
        } else {
            retryLoop.execute(policy = retryPolicy.resolve(), strategy = retryStrategy) {
                runAttempt(stampedRequest, timeoutMs, requestId)
            }
        }
    }

    private suspend fun runAttempt(
        request: Request,
        timeoutMs: Long?,
        requestId: String,
    ): HttpAttemptResult =
        try {
            val response =
                if (timeoutMs != null) {
                    // withTimeoutOrNull only swallows its OWN timeout: a caller's enclosing
                    // withTimeout still propagates as cancellation below. The body is buffered
                    // inside the block so a late-arriving body counts against the attempt budget.
                    withTimeoutOrNull(timeoutMs) {
                        okHttpClient.newCall(request).await().toRawHttpResponse(requestId)
                    } ?: return HttpAttemptResult.TransportError(HttpRequestTimeoutException(timeoutMs = timeoutMs))
                } else {
                    okHttpClient.newCall(request).await().toRawHttpResponse(requestId)
                }
            HttpAttemptResult.Response(response)
        } catch (expected: CancellationException) {
            throw expected
        } catch (expected: IOException) {
            HttpAttemptResult.TransportError(expected)
        }

    private fun Response.toRawHttpResponse(requestId: String): RawHttpResponse {
        val bodyText = use { response -> response.body?.string() }
        return RawHttpResponse(
            requestId = requestId,
            statusCode = code,
            headers = headers.toMultimap(),
            bodyText = bodyText,
        )
    }

    private fun RawHttpRequest.toOkHttpRequest(): Request {
        val mediaType =
            headers.entries
                .firstOrNull { (name, _) -> name.equals(HEADER_CONTENT_TYPE, ignoreCase = true) }
                ?.value
                ?.toMediaTypeOrNull()
                ?: CONTENT_TYPE_APPLICATION_JSON.toMediaType()
        val builder = Request.Builder().url(url)
        headers.forEach { (name, value) -> builder.header(name, value) }
        // OkHttp rejects a null body for POST/PUT/PATCH, but the wire contract allows bodyless
        // steps for any method - send an empty body instead (matching web fetch).
        val requestBody = body?.toRequestBody(mediaType)
            ?: EMPTY_BODY.takeIf { method in METHODS_REQUIRING_BODY }
        return builder
            .method(method.name, requestBody)
            .build()
    }

    companion object {
        const val HEADER_REQUEST_ID = "X-Request-Id"
        private const val HEADER_CONTENT_TYPE = "Content-Type"
        private val METHODS_REQUIRING_BODY = setOf(RawHttpMethod.POST, RawHttpMethod.PUT, RawHttpMethod.PATCH)
        private val EMPTY_BODY = ByteArray(0).toRequestBody()
    }
}
