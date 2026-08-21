package io.primer.android.core.data.network.transport

private const val MIN_SUCCESS_STATUS_CODE = 200
private const val MAX_SUCCESS_STATUS_CODE = 299
private val SUCCESS_STATUS_CODES = MIN_SUCCESS_STATUS_CODE..MAX_SUCCESS_STATUS_CODE

/** A settled HTTP response with its body buffered once as text. */
data class RawHttpResponse(
    val requestId: String,
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val bodyText: String?,
) {
    val isSuccess: Boolean get() = statusCode in SUCCESS_STATUS_CODES
}
