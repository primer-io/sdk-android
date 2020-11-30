package io.primer.android.api

import android.os.Handler
import android.os.Looper
import io.primer.android.session.ClientToken

import io.primer.android.logging.Logger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


class APIClient(token: ClientToken) : IAPIClient {
  private val log = Logger("api-client")
  private val client = OkHttpClient()
  private val handler = Handler(Looper.getMainLooper())
  private var clientToken = token

  override fun get(url: String): Observable {
    return this.request(Request.Builder().get().url(url))
  }

  override fun post(url: String, body: JSONObject?): Observable {
    return this.request(Request.Builder().post(toRequestBody(body)).url(url))
  }

  private fun request(builder: Request.Builder): Observable {
    val request = builder
      .addHeader("Content-Type", "application/json")
      .addHeader("Primer-SDK-Version", "1.0.0-beta.0")
      .addHeader("Primer-SDK-Client", "ANDROID_NATIVE")
      .addHeader("Primer-Client-Token", clientToken.accessToken)
      .build()

    val observable = Observable()

    client.newCall(request).enqueue(object: Callback {
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

    return observable
  }

  private fun getJSON(response: Response): JSONObject {
    return JSONObject(response.body?.string() ?: "{}")
  }

  private fun toRequestBody(json: JSONObject?): RequestBody {
    val stringified = if (json == null) "{}" else json.toString();
    val mediaType = "application/json".toMediaType()
    return stringified.toRequestBody(mediaType)
  }
}