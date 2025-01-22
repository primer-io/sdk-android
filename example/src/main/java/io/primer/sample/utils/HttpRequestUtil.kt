package io.primer.sample.utils

import com.google.gson.GsonBuilder
import io.primer.sample.datamodels.ExampleAppRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object HttpRequestUtil {
    fun generateRequest(
        body: ExampleAppRequestBody,
        uri: String,
        environment: String,
        apiKey: String? = null
    ): Request {
        val mimeType = "application/json".toMediaType()
        val gson = GsonBuilder()
            .registerTypeAdapter(Map::class.java, MapDeserializer)
            .create()
        val json = gson.toJson(body)
        val reqBody = json.toRequestBody(mimeType)
        val requestBuilder = Request.Builder()
            .url(uri)
            .header("X-Api-Version", "2.4")
            .header("environment", environment)
            .post(reqBody)
        if (!apiKey.isNullOrBlank()) requestBuilder.addHeader("X-Api-Key", apiKey)
        return requestBuilder.build()
    }
}
