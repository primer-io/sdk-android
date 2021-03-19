package io.primer.android.model

import android.os.Handler
import android.os.Looper
import io.primer.android.BuildConfig
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.logging.Logger
import io.primer.android.model.dto.APIError
import io.primer.android.model.dto.ClientToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

internal interface IAPIClient {

    fun get(url: String): Observable

    fun post(url: String, body: JSONObject?): Observable

    fun delete(url: String): Observable
}

internal class APIClient(token: ClientToken) : IAPIClient {

    private val log = Logger("api-client")
    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    private val clientToken = token

    // all these 3 functions are only called from Model.kt

    override fun get(url: String): Observable {
        return request(Request.Builder().get().url(url))
    }

    override fun post(url: String, body: JSONObject?): Observable {
        return request(Request.Builder().post(toRequestBody(body)).url(url))
    }

    override fun delete(url: String): Observable {
        return request(Request.Builder().delete().url(url))
    }

    private fun request(builder: Request.Builder): Observable {
        val request = builder
            .addHeader("Content-Type", "application/json")
            .addHeader("Primer-SDK-Version", BuildConfig.SDK_VERSION_STRING)
            .addHeader("Primer-SDK-Client", "ANDROID_NATIVE")
            .addHeader("Primer-Client-Token", clientToken.accessToken)
            .build()

        val observable = Observable()

        val thread = Thread {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    handler.post {
                        observable.setError(APIError.create(e))
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    handler.post {
                        if (response.code != 200) {
                            observable.setError(APIError.create(response))
                        } else {
                            observable.setSuccess(getJSON(response))
                        }
                    }
                }
            })
        }

        thread.start()

        return observable.observe {
            if (it is Observable.ObservableErrorEvent) {
                EventBus.broadcast(CheckoutEvent.ApiError(it.error))
            }
        }
    }

    private fun getJSON(response: Response): JSONObject {
        return JSONObject(response.body?.string() ?: "{}")
    }

    private fun toRequestBody(json: JSONObject?): RequestBody {
        val stringified = json?.toString() ?: "{}"
        val mediaType = "application/json".toMediaType()
        return stringified.toRequestBody(mediaType)
    }
}
