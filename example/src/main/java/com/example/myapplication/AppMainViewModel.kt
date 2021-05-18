package com.example.myapplication

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
import java.io.IOException

private const val BACKEND_ROOT: String = "https://us-central1-primerdemo-8741b.cloudfunctions.net"
private const val CLIENT_TOKEN_URI: String = "$BACKEND_ROOT/clientToken"
private const val TRANSACTION_URI: String = "$BACKEND_ROOT/transaction"

enum class TransactionState { SUCCESS, ERROR, IDLE }

class AppMainViewModel : ViewModel() {

    private val client = OkHttpClient()

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
        val mediaType = "application/json; charset=utf-8"
        val currentUserId = "customer8"
        val body = ClientTokenRequest(currentUserId)
        val json = Gson().toJson(body)
        val request = Request.Builder()
            .url(CLIENT_TOKEN_URI)
            .post(RequestBody.create(MediaType.get(mediaType), json.toString()))
            .build()

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
            .url(TRANSACTION_URI)
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

data class ClientTokenRequest(@SerializedName("customerId") val id: String)
data class ClientTokenResponse(val clientToken: String, val expirationDate: String)

data class TransactionRequest(
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("capture") val capture: Boolean,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("type") val type: String,
)

data class TransactionResponse(val message: String)

//
//class ClientTokenRequest(
//    onSuccess: Response.Listener<JSONObject>,
//    onError: Response.ErrorListener,
//) : JsonObjectRequest(
//    Method.POST,
//    CLIENT_TOKEN_URI,
//    JSONObject().apply { put("customerId", CUSTOMER_ID) },
//    onSuccess,
//    onError,
//) {
//
//    override fun getHeaders(): MutableMap<String, String> =
//        HashMap<String, String>().apply {
//            if (API_KEY.isNotEmpty()) {
//                put("X-Api-Key", API_KEY)
//            }
//        }
//
//    override fun getBody(): ByteArray {
//        val body = """
//            {
//                "staging": true,
//                "customerId": "hCYs6vHqYCa7o3893C4s9Y464P13",
//                "checkout": {
//                    "paymentFlow": "PREFER_VAULT"
//                }
//            }
//        """.trimIndent()
//        return body.toByteArray()
//    }
//}