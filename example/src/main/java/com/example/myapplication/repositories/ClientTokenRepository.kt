package com.example.myapplication.repositories

import com.example.myapplication.constants.PrimerRoutes
import com.example.myapplication.datamodels.ClientTokenRequest
import com.example.myapplication.datamodels.ClientTokenResponse
import com.example.myapplication.datamodels.type
import com.example.myapplication.datasources.ApiKeyDataSource
import com.example.myapplication.utils.HttpRequestUtil
import com.google.gson.GsonBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class ClientTokenRepository(private val apiKeyDataSource: ApiKeyDataSource) {

    fun fetch(
        customerId: String,
        environment: String,
        countryCode: String,
        client: OkHttpClient,
        callback: (token: String?) -> Unit,
    ) {
        val body = ClientTokenRequest(customerId, environment, countryCode)
        val request = HttpRequestUtil.generateRequest(
            body,
            PrimerRoutes.clientToken,
            environment,
            apiKey = apiKeyDataSource.getApiKey(environment.type())
        )
        client.cache?.delete()
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val tokenResponse = GsonBuilder()
                        .create()
                        .fromJson(response.body?.string(), ClientTokenResponse::class.java)

                    callback(tokenResponse.clientToken)
                }
            }
        })
    }
}