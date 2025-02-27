package io.primer.sample.repositories

import com.google.gson.GsonBuilder
import io.primer.sample.constants.PrimerRoutes
import io.primer.sample.datamodels.ClientSession
import io.primer.sample.datamodels.type
import io.primer.sample.datasources.ApiKeyDataSource
import io.primer.sample.utils.HttpRequestUtil
import io.primer.sample.utils.MapDeserializer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class ClientSessionRepository(
    private val apiKeyDataSource: ApiKeyDataSource
) {

    fun fetch(
        client: OkHttpClient,
        customerId: String,
        orderId: String,
        amount: Int,
        countryCode: String,
        currency: String,
        environment: String,
        metadata: String?,
        captureVaultedCardCvv: Boolean,
        vaultOnSuccess: Boolean? = null,
        vaultOnAgreement: Boolean? = null,
        callback: (token: String?) -> Unit,
    ) {
        val body = ClientSession.Request.build(
            customerId = customerId,
            orderId = orderId,
            amount = amount,
            countryCode = countryCode,
            currency = currency,
            metadata = metadata,
            captureVaultedCardCvv = captureVaultedCardCvv,
            vaultOnSuccess = vaultOnSuccess,
            vaultOnAgreement = vaultOnAgreement
        )
        val request = HttpRequestUtil.generateRequest(
            body,
            PrimerRoutes.clientSession,
            environment,
            apiKeyDataSource.getApiKey(environment.type())
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
                        .registerTypeAdapter(Map::class.java, MapDeserializer)
                        .create()
                        .fromJson(response.body?.string(), ClientSession.Response::class.java)

                    callback(tokenResponse.clientToken)
                }
            }
        })
    }
}
