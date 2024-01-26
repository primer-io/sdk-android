package io.primer.android.core.extensions

import okio.Buffer
import java.io.EOFException

/**
 * Returns true if the body in question probably contains human readable text. Uses a small
 * sample of code points to detect unicode control characters commonly used in binary file
 * signatures. Based on OkHttp implementation.
 * See https://github.com/square/okhttp/blob/0fea18494cd1fc9714596fbea8dd1563421ecef9/okhttp-logging-interceptor/src/main/kotlin/okhttp3/logging/internal/utf8.kt
 */
@Suppress("MagicNumber")
internal fun Buffer.isProbablyUtf8(): Boolean =
    try {
        val prefix = Buffer()
        val byteCount = size.coerceAtMost(64)
        copyTo(prefix, 0, byteCount)
        !(0 until 16)
            .asSequence()
            .takeWhile { !prefix.exhausted() }
            .map { prefix.readUtf8CodePoint() }
            .any { codePoint ->
                Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)
            }
    } catch (_: EOFException) {
        false // Truncated UTF-8 sequence.
    }
