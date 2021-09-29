package com.example.myapplication.utils

import com.example.myapplication.datamodels.ExampleAppRequestBody
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

class HttpRequestUtil {
    companion object {
        fun generateRequest(body: ExampleAppRequestBody, uri: String): Request {
            val mimeType = MediaType.get("application/json")
            val json = Gson().toJson(body)
            val reqBody = RequestBody.create(mimeType, json)
            return Request.Builder()
                .url(uri)
                .post(reqBody)
                .build()
        }
    }
}
