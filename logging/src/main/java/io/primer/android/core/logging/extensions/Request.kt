package io.primer.android.core.logging.extensions

import io.primer.android.core.logging.internal.HttpLoggerInterceptor
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.serialization.json.extensions.update
import okhttp3.Request
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject

@Suppress("LongParameterList")
internal fun Request.logHeaders(
    logReporter: LogReporter,
    shouldLogHeaders: Boolean,
    blacklistedHeaders: List<String>,
    obfuscationLevel: HttpLoggerInterceptor.ObfuscationLevel,
    obfuscationString: String,
) {
    val request = this
    val requestBody = request.body
    val stringBuilder =
        StringBuilder(
            "--> ${request.method} ${request.url}" +
                request.body?.let { " (${it.contentLength()}-byte body)" }.orEmpty(),
        )
    if (shouldLogHeaders) {
        if (requestBody != null) {
            stringBuilder.append("\nContent-Type: ${requestBody.contentType()}")
                .append("\nContent-Length: ${requestBody.contentLength()}")
        }

        request.headers.forEach {
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
internal fun Request.logBody(
    logReporter: LogReporter,
    shouldLogBody: Boolean,
    whitelistedBodyKeys: List<WhitelistedKey>,
    obfuscationLevel: HttpLoggerInterceptor.ObfuscationLevel,
    obfuscationString: String,
) {
    val requestBody = body
    val stringBuilder = StringBuilder("--> END $method")

    if (shouldLogBody && requestBody != null) {
        if (obfuscationLevel == HttpLoggerInterceptor.ObfuscationLevel.REDACT_BODY) {
            stringBuilder.insert(0, "[sensitive data]\n")
        } else {
            stringBuilder.append(" ")
            when {
                headers.bodyHasUnknownEncoding() -> {
                    stringBuilder.append("(encoded body omitted)")
                }
                requestBody.isDuplex() -> {
                    stringBuilder.append("(duplex request body omitted)")
                }
                requestBody.isOneShot() -> {
                    stringBuilder.append("(one-shot body omitted)")
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
    }
    logReporter.debug(stringBuilder.toString())
}

internal fun Request.appendKnownEncodingBody(
    stringBuilder: StringBuilder,
    shouldObfuscate: Boolean,
    whitelistedBodyKeys: List<WhitelistedKey>,
    obfuscationString: String,
) {
    val requestBody = requireNotNull(body)
    val buffer = Buffer().also { requestBody.writeTo(it) }
    if (buffer.isProbablyUtf8()) {
        var json = buffer.clone().readString(requestBody.contentType().charset)

        if (shouldObfuscate) {
            runCatching { JSONObject(json) }.getOrNull()?.let { jsonObject ->
                jsonObject.update(whitelistedBodyKeys, obfuscationString)
                json = jsonObject.toString()
            } ?: runCatching { JSONArray(json) }.getOrNull()?.let { jsonArray ->
                jsonArray.update(whitelistedBodyKeys, obfuscationString)
                json = jsonArray.toString()
            }
        }

        stringBuilder.insert(0, "$json\n")
        stringBuilder.append("(${requestBody.contentLength()}-byte body)")
    } else {
        stringBuilder.append("(binary ${requestBody.contentLength()}-byte body omitted)")
    }
}
