package io.primer.sample.repositories

import com.google.gson.GsonBuilder
import io.primer.sample.constants.PrimerRoutes
import io.primer.sample.datamodels.ResumePaymentRequest
import io.primer.sample.datamodels.TransactionResponse
import io.primer.sample.datamodels.TransactionStatus
import io.primer.sample.datamodels.type
import io.primer.sample.datasources.ApiKeyDataSource
import io.primer.sample.utils.HttpRequestUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class ResumeRepository(private val apiKeyDataSource: ApiKeyDataSource) {

    fun create(
        id: String,
        body: ResumePaymentRequest,
        environment: String,
        client: OkHttpClient,
        callback: (TransactionResponse) -> Unit,
        stateCallback: (TransactionStatus) -> Unit,
    ) {
        val request = HttpRequestUtil.generateRequest(
            body,
            PrimerRoutes.buildResumePaymentsUrl(id),
            environment,
            apiKey = apiKeyDataSource.getApiKey(environment.type())
        )
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(
                call: Call,
                e: IOException
            ) = stateCallback(TransactionStatus.FAILED)

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val transactionResponse = GsonBuilder()
                            .setLenient()
                            .create()
                            .fromJson(response.body?.string(), TransactionResponse::class.java)
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