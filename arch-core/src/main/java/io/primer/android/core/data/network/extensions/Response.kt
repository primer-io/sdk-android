package io.primer.android.core.data.network.extensions

import io.primer.android.core.data.serialization.json.JSONDataUtils
import io.primer.android.core.data.serialization.json.JSONDataUtils.stringToJsonData
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response

fun Response.containsError(): Boolean =
    if (isSuccessful) {
        val actualBody = peekBody(Long.MAX_VALUE)
        if (actualBody.contentType() != "application/json".toMediaType()) {
            false
        } else {
            when (val json = stringToJsonData(actualBody.string())) {
                is JSONDataUtils.JSONData.JSONObjectData -> json.json.has("error")
                is JSONDataUtils.JSONData.JSONArrayData -> false
            }
        }
    } else {
        true
    }
