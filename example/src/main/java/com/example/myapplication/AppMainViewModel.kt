package com.example.myapplication

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

enum class TransactionState { SUCCESS, ERROR, IDLE }

@Keep
class AppMainViewModel : ViewModel() {

    private val root: String = "https://us-central1-primerdemo-8741b.cloudfunctions.net"
    private val clientTokenUri: String = "$root/clientToken"
    private val transactionUri: String = "$root/transaction"

    private val client: OkHttpClient = OkHttpClient()

    private val _clientToken: MutableLiveData<String?> = MutableLiveData<String?>()

    val clientToken: LiveData<String?> = _clientToken

    private val _transactionState: MutableLiveData<TransactionState> =
        MutableLiveData(TransactionState.IDLE)

    val transactionState: LiveData<TransactionState> = _transactionState

    fun resetTransactionState(): Unit = _transactionState.postValue(TransactionState.IDLE)

    init {
        fetchClientToken()
    }

    private fun fetchClientToken() {
        val mimeType = MediaType.get("application/json")
        val body = ClientTokenRequest("customer8", "SE", "sandbox")
        val json = Gson().toJson(body)

        val reqBody = RequestBody.create(mimeType, json)
        val request = Request.Builder()
            .url(clientTokenUri)
            .post(reqBody)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val stringified = response.body()?.string()

                    val tokenResponse = GsonBuilder()
                        .create()
                        .fromJson(stringified, ClientTokenResponse::class.java)

                    _clientToken.postValue(tokenResponse.clientToken)
                }
            }
        })
    }

    fun createTransaction(
        paymentMethod: String,
        amount: Int,
        capture: Boolean,
        currencyCode: String,
        type: String,
    ) {
        val mediaType = "application/json; charset=utf-8"
        val body = TransactionRequest(paymentMethod, amount, capture, currencyCode, type)
        val json = Gson().toJson(body)

        val request = Request.Builder()
            .url(transactionUri)
            .post(RequestBody.create(MediaType.get(mediaType), json.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        _transactionState.postValue(TransactionState.SUCCESS)
                    } else {
                        _transactionState.postValue(TransactionState.ERROR)
                    }
                }
            }
        })
    }
}

@Keep
data class ClientTokenRequest(
    @SerializedName("customerId") val id: String,
    @SerializedName("customerCountryCode") val countryCode: String,
    @SerializedName("environment") val environment: String,
)

@Keep
data class ClientTokenResponse(val clientToken: String, val expirationDate: String)

@Keep
data class TransactionRequest(
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("capture") val capture: Boolean,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("type") val type: String,
)

//data class TransactionResponse(val message: String)
