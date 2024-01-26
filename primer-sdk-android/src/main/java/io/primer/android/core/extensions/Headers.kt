package io.primer.android.core.extensions

import okhttp3.Headers

internal fun Headers.bodyHasUnknownEncoding(): Boolean {
    val contentEncoding = this["Content-Encoding"] ?: return false
    return !contentEncoding.equals("identity", ignoreCase = true) &&
        !contentEncoding.equals("gzip", ignoreCase = true)
}
