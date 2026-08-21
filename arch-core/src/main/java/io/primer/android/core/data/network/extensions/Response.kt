package io.primer.android.core.data.network.extensions

import io.primer.android.core.data.network.CONTENT_TYPE_APPLICATION_JSON
import io.primer.android.core.data.network.transport.RawHttpResponse
import io.primer.android.core.data.serialization.json.JSONDataUtils
import io.primer.android.core.data.serialization.json.JSONDataUtils.stringToJsonData
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response

private const val CONTENT_TYPE_HEADER = "content-type"
private const val ERROR_FIELD = "error"

fun Response.containsError(): Boolean =
    if (isSuccessful) {
        val actualBody = peekBody(Long.MAX_VALUE)
        if (actualBody.contentType() != "application/json".toMediaType()) {
            false
        } else {
            when (val json = stringToJsonData(actualBody.string())) {
                is JSONDataUtils.JSONData.JSONObjectData -> json.json.has(ERROR_FIELD)
                is JSONDataUtils.JSONData.JSONArrayData -> false
            }
        }
    } else {
        true
    }

/**
 * Mirrors [Response.containsError] for the raw execution path: a 2xx response whose
 * `Content-Type` is exactly `application/json` and whose body is a JSON object with an `error`
 * field is treated as an error.
 */
fun RawHttpResponse.containsError(): Boolean =
    if (isSuccess) {
        val mediaType =
            headers.entries
                .firstOrNull { (name, _) -> name.equals(CONTENT_TYPE_HEADER, ignoreCase = true) }
                ?.value
                ?.firstOrNull()
                ?.toMediaTypeOrNull()
        if (mediaType != CONTENT_TYPE_APPLICATION_JSON.toMediaType()) {
            false
        } else {
            when (val json = stringToJsonData(bodyText.orEmpty())) {
                is JSONDataUtils.JSONData.JSONObjectData -> json.json.has(ERROR_FIELD)
                is JSONDataUtils.JSONData.JSONArrayData -> false
            }
        }
    } else {
        true
    }
