package com.example.myapplication.repositories

import com.example.myapplication.constants.PrimerRoutes
import com.example.myapplication.datamodels.TransactionRequest
import com.example.myapplication.datamodels.TransactionResponse
import com.example.myapplication.datamodels.TransactionStatus
import com.example.myapplication.utils.HttpRequestUtil
import com.google.gson.GsonBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class PaymentsRepository {

    fun create(
        body: TransactionRequest,
        environment: String,
        client: OkHttpClient,
        callback: (TransactionResponse) -> Unit,
        stateCallback: (TransactionStatus) -> Unit,
    ) {
        val request =
            HttpRequestUtil.generateRequest(body, PrimerRoutes.payments, environment, true)
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(
                call: Call,
                e: IOException
            ) = stateCallback(TransactionStatus.FAILED)

            override fun onResponse(call: Call, response: Response) {

                response.use {
                    if (response.isSuccessful) {

                        val res = response.body()?.string()

                        val transactionResponse = GsonBuilder()
                            .setLenient()
                            .create()
                            .fromJson(res, TransactionResponse::class.java)

                        callback(transactionResponse)
                        stateCallback(transactionResponse.status)
                    } else {
                        stateCallback(TransactionStatus.FAILED)
                    }
                }
            }
        })
    }
}