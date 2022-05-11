package com.example.myapplication.repositories

import com.example.myapplication.constants.PrimerRoutes
import com.example.myapplication.datamodels.PaymentInstrumentsResponse
import com.example.myapplication.utils.HttpRequestUtil
import com.google.gson.GsonBuilder
import io.primer.android.model.dto.PrimerPaymentMethodTokenData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class PaymentInstrumentsRepository {

    fun fetch(
        customerId: String,
        environment: String,
        client: OkHttpClient,
        callback: (tokens: List<PrimerPaymentMethodTokenData>) -> Unit,
    ) {
        val request = HttpRequestUtil.generateGetRequest(
            PrimerRoutes.buildPaymentInstrumentsUrl(customerId),
            environment,
            true
        )
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
                        .fromJson(response.body()?.string(), PaymentInstrumentsResponse::class.java)

                    callback(tokenResponse.data)
                }
            }
        })
    }
}