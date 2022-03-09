package com.example.myapplication.utils

import com.example.myapplication.datamodels.ExampleAppRequestBody
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

class HttpRequestUtil {
    companion object {

        private const val API_KEY = "2b8537b2-478c-4315-88fb-f1954500b5fa"

        fun generateGetRequest(
            uri: String,
            environment: String,
            useOldVersion: Boolean = false
        ): Request {
            return Request.Builder()
                .url(uri)
                .header("X-Api-Version", if (useOldVersion) "2021-09-27" else "2021-10-19")
                .header("environment", environment)
                .header("x-api-key", API_KEY)
                .get()
                .build()
        }


        fun generateRequest(
            body: ExampleAppRequestBody,
            uri: String,
            environment: String,
            useOldVersion: Boolean = false
        ): Request {
            val mimeType = MediaType.get("application/json")
            val json = Gson().toJson(body)
            val reqBody = RequestBody.create(mimeType, json)
            return Request.Builder()
                .url(uri)
                .header("X-Api-Version", if (useOldVersion) "2021-09-27" else "2021-10-19")
                .header("environment", environment)
                .header("x-api-key", API_KEY)
                .post(reqBody)
                .build()
        }
    }
}
