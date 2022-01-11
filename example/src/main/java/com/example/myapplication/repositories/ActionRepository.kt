package com.example.myapplication.repositories

import com.example.myapplication.constants.PrimerRoutes
import com.example.myapplication.datamodels.Action
import com.example.myapplication.datamodels.ClientSession
import com.example.myapplication.datamodels.ExampleAppRequestBody
import com.example.myapplication.utils.HttpRequestUtil
import com.google.gson.GsonBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class ActionRepository {

    fun post(
        body: Action.Request,
        client: OkHttpClient,
        environment: String,
        callback: (token: String?) -> Unit,
    ) {
        val request = HttpRequestUtil.generateRequest(body, PrimerRoutes.actions, environment)

        client.cache()?.delete()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }

                    val tokenResponse = GsonBuilder()
                        .create()
                        .fromJson(response.body()?.string(), ClientSession.Response::class.java)

                    callback(tokenResponse.clientToken)
                }
            }
        })
    }
}