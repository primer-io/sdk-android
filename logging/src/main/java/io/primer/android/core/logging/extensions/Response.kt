package io.primer.android.core.logging.extensions

import io.primer.android.core.logging.internal.HttpLoggerInterceptor
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.serialization.json.extensions.update
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import okio.GzipSource
import org.json.JSONArray
import org.json.JSONObject

@Suppress("LongParameterList")
internal fun Response.logHeaders(
    logReporter: LogReporter,
    requestTime: Long,
    shouldLogHeaders: Boolean,
    blacklistedHeaders: List<String>,
    obfuscationLevel: HttpLoggerInterceptor.ObfuscationLevel,
    obfuscationString: String,
) {
    val response = this
    val responseBody = response.body

    val contentLength = response.body?.contentLength() ?: -1
    val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"

    val stringBuilder =
        StringBuilder(
            "<-- ${response.code} ${request.method} ${request.url}" +
                " (${requestTime}ms, $bodySize body)",
        )

    if (shouldLogHeaders) {
        if (responseBody != null) {
            stringBuilder.append("\nContent-Type: ${responseBody.contentType()}")
                .append("\nContent-Length: ${responseBody.contentLength()}")
        }

        response.headers.forEach {
            it.appendHeader(
                stringBuilder = stringBuilder,
                excludedHeaders = setOf("content-type", "content-length"),
                shouldObfuscate = obfuscationLevel == HttpLoggerInterceptor.ObfuscationLevel.LIST,
                blacklistedHeaders = blacklistedHeaders,
                obfuscationString = obfuscationString,
            )
        }
    }
    logReporter.debug(stringBuilder.toString())
}

@Suppress("LongParameterList")
internal fun Response.logBody(
    logReporter: LogReporter,
    shouldLogBody: Boolean,
    whitelistedBodyKeys: List<WhitelistedKey>,
    obfuscationLevel: HttpLoggerInterceptor.ObfuscationLevel,
    obfuscationString: String,
) {
    val errorBody =
        if (isSuccessful) {
            null
        } else {
            peekBody(Long.MAX_VALUE).string()
        }

    val stringBuilder = StringBuilder()

    if (shouldLogBody) {
        if (obfuscationLevel == HttpLoggerInterceptor.ObfuscationLevel.REDACT_BODY) {
            stringBuilder.appendLine("[sensitive data]")
                .append("<-- END HTTP")
        } else {
            when {
                errorBody != null -> {
                    stringBuilder.appendLine("Network Error Response: $errorBody")
                        .append("<-- END HTTP")
                }
                !promisesBody() -> {
                    stringBuilder.append("<-- END HTTP")
                }
                headers.bodyHasUnknownEncoding() -> {
                    stringBuilder.append("<-- END HTTP (encoded body omitted)")
                }
                else -> {
                    appendKnownEncodingBody(
                        stringBuilder = stringBuilder,
                        shouldObfuscate = obfuscationLevel == HttpLoggerInterceptor.ObfuscationLevel.LIST,
                        whitelistedBodyKeys = whitelistedBodyKeys,
                        obfuscationString = obfuscationString,
                    )
                }
            }
        }
    } else {
        stringBuilder.append("<-- END HTTP")
    }

    logReporter.debug(stringBuilder.toString())
}

private fun Response.appendKnownEncodingBody(
    stringBuilder: StringBuilder,
    shouldObfuscate: Boolean,
    whitelistedBodyKeys: List<WhitelistedKey>,
    obfuscationString: String,
) {
    val responseBody = requireNotNull(body)
    val source = responseBody.source()
    source.request(Long.MAX_VALUE) // Buffer the entire body.
    var buffer = source.buffer

    var gzippedLength: Long? = null
    if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
        gzippedLength = buffer.size
        GzipSource(buffer.clone()).use { gzippedResponseBody ->
            buffer = Buffer().apply { writeAll(gzippedResponseBody) }
        }
    }

    val contentType = responseBody.contentType()

    if (buffer.isProbablyUtf8()) {
        if (responseBody.contentLength() != 0L) {
            var json = buffer.clone().readString(contentType.charset)

            if (shouldObfuscate) {
                runCatching { JSONObject(json) }.getOrNull()?.let { jsonObject ->
                    jsonObject.update(whitelistedBodyKeys, obfuscationString)
                    json = jsonObject.toString()
                } ?: runCatching { JSONArray(json) }.getOrNull()?.let { jsonArray ->
                    jsonArray.update(whitelistedBodyKeys, obfuscationString)
                    json = jsonArray.toString()
                }
            }

            stringBuilder.appendLine(json)
        }

        if (gzippedLength != null) {
            stringBuilder.append(
                "<-- END HTTP (${buffer.size}-byte, $gzippedLength-gzipped-byte body)",
            )
        } else {
            stringBuilder.append("<-- END HTTP (${buffer.size}-byte body)")
        }
    } else {
        stringBuilder.append("<-- END HTTP (binary ${buffer.size}-byte body omitted)")
    }
}
