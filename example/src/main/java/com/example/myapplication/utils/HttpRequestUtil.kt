package com.example.myapplication.utils

import com.example.myapplication.datamodels.ExampleAppRequestBody
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class HttpRequestUtil {
    companion object {

        fun generateGetRequest(
            uri: String,
            environment: String,
            useOldVersion: Boolean = false,
            apiKey: String? = null
        ): Request {
            val requestBuilder = Request.Builder()
                .url(uri)
                .header("X-Api-Version", if (useOldVersion) "2021-09-27" else "2021-10-19")
                .header("environment", environment)
                .get()
            if (!apiKey.isNullOrBlank()) requestBuilder.addHeader("X-Api-Key", apiKey)
            return requestBuilder.build()
        }

        fun generateRequest(
            body: ExampleAppRequestBody,
            uri: String,
            environment: String,
            useOldVersion: Boolean = false,
            apiKey: String? = null
        ): Request {
            val mimeType = "application/json".toMediaType()
            val json = Gson().toJson(body)
            val reqBody = json.toRequestBody(mimeType)
            val requestBuilder = Request.Builder()
                .url(uri)
                .header("X-Api-Version", if (useOldVersion) "2021-09-27" else "2021-10-19")
                .header("environment", environment)
                .post(reqBody)
            if (!apiKey.isNullOrBlank()) requestBuilder.addHeader("X-Api-Key", apiKey)
            return requestBuilder.build()
        }
    }
}
