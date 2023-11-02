package io.primer.android.core.logging.internal

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

internal class HttpLoggerInterceptor(private val logReporter: LogReporter) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestStartTime = System.currentTimeMillis()

        logReporter.apply {
            debug("---> ${request.method()} ${request.url()}")
            request.body()?.let { requestBody ->
                debug("Content-Type: ${requestBody.contentType()}")
                debug("Content-Length: ${requestBody.contentLength()}")
            }
            debug("---> END ${request.method()}")
        }

        val response: Response?
        try {
            response = chain.proceed(request)

            val errorBody = if (response?.isSuccessful == true) {
                null
            } else {
                response.peekBody(Long.MAX_VALUE).string()
            }

            logReporter.apply {
                debug(
                    "<--- ${response.code()} ${request.method()} ${request.url()}" +
                        " (${System.currentTimeMillis() - requestStartTime}ms)"
                )
                response.body()?.let { responseBody ->
                    debug("Content-Type: ${responseBody.contentType()}")
                    debug("Content-Length: ${responseBody.contentLength()}")
                }
                errorBody?.let { body ->
                    debug("Network Error Response: $body")
                }
                debug("<--- END HTTP")
            }
        } catch (e: IOException) {
            logReporter.apply {
                debug("<--- ${request.method()} ${request.url()}")
                debug("HTTP failed: ${e.message}")
            }
            throw e
        }

        return response
    }
}
