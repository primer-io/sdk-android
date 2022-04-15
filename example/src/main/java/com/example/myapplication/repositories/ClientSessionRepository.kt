package com.example.myapplication.repositories

import com.example.myapplication.constants.PrimerRoutes
import com.example.myapplication.datamodels.ClientSession
import com.example.myapplication.utils.HttpRequestUtil
import com.google.gson.GsonBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class ClientSessionRepository(private val apiKeyDataSource: ApiKeyDataSource) {

    fun fetch(
        client: OkHttpClient,
        customerId: String,
        orderId: String,
        amount: Int,
        countryCode: String,
        currency: String,
        environment: String,
        callback: (token: String?) -> Unit,
    ) {
        val body = ClientSession.Request.build(customerId, orderId, amount, countryCode, currency)
        val request = HttpRequestUtil.generateRequest(
            body,
            PrimerRoutes.clientSession,
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
                        .fromJson(response.body?.string(), ClientSession.Response::class.java)

                    callback(tokenResponse.clientToken)
                }
            }
        })
    }
}