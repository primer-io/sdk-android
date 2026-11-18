package io.primer.android.core.data.network.utils

import io.primer.android.core.data.network.exception.InvalidUrlException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private const val PRIMER_DOMAIN = "primer.io"

/**
 * True when this is an HTTPS URL whose host is `primer.io` or one of its subdomains. Client tokens
 * are not signature-checked on the device, so any endpoint taken from one must pass this before use.
 */
fun String.isPrimerUrl(): Boolean {
    val url = toHttpUrlOrNull() ?: return false
    return url.scheme == "https" && (url.host == PRIMER_DOMAIN || url.host.endsWith(".$PRIMER_DOMAIN"))
}

/**
 * Returns this URL when it is a Primer URL and throws [InvalidUrlException] otherwise. Use it on resume-token
 * endpoints right before the request: the shared HTTP client attaches the access token to every call.
 */
@Throws(InvalidUrlException::class)
fun String.requirePrimerUrl(): String = if (isPrimerUrl()) this else throw InvalidUrlException(url = this)
