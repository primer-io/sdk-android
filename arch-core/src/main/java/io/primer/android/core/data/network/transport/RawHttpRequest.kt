package io.primer.android.core.data.network.transport

/**
 * A transport-level HTTP request.
 *
 * @param body pre-serialized JSON; the caller sets the `Content-Type` header (defaults to
 * `application/json` when absent).
 * @param timeoutMs optional per-attempt timeout, owned by the transport.
 * @param requestId stamped as `X-Request-Id` on every attempt of this logical request.
 */
data class RawHttpRequest(
    val method: RawHttpMethod,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val timeoutMs: Long? = null,
    val requestId: String,
)

enum class RawHttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
}
