package com.example.myapplication.repositories

import com.example.myapplication.constants.PrimerRoutes
import com.example.myapplication.datamodels.ClientSessionRequest
import com.example.myapplication.datamodels.ClientTokenResponse
import com.example.myapplication.datamodels.CustomerRequest
import com.example.myapplication.datamodels.OrderRequest
import com.example.myapplication.utils.HttpRequestUtil
import com.google.gson.GsonBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class ClientSessionRepository {

    fun fetch(
        customerId: String,
        orderId: String,
        environment: String,
        amount: Int,
        currencyCode: String,
        customer: CustomerRequest? = null,
        order: OrderRequest? = null,
        client: OkHttpClient,
        callback: (token: String?) -> Unit,
    ) {
        val body =
            ClientSessionRequest(
                customerId,
                orderId,
                environment,
                amount,
                currencyCode,
                customer,
                order
            )
        val request = HttpRequestUtil.generateRequest(body, PrimerRoutes.clientSession)
        client.cache()?.delete()
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val tokenResponse = GsonBuilder()
                        .create()
                        .fromJson(response.body()?.string(), ClientTokenResponse::class.java)

                    callback(tokenResponse.clientToken)
                }
            }
        })
    }
}