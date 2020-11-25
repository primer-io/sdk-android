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

  override fun get(
    url: String,
    callback: ((APISuccessResponse) -> Unit),
    onError: ((APIErrorResponse) -> Unit)
  ) {
    this.request(Request.Builder().get().url(url), callback, onError)
  }

  override fun post(
    url: String,
    body: JSONObject?,
    callback: ((APISuccessResponse) -> Unit),
    onError: ((APIErrorResponse) -> Unit)
  ) {
    this.request(Request.Builder().post(toRequestBody(body)).url(url), callback, onError)
  }

  private fun request(
    builder: Request.Builder,
    callback: ((APISuccessResponse) -> Unit),
    onError: ((APIErrorResponse) -> Unit)
  ) {
    val request = builder
      .addHeader("Content-Type", "application/json")
      .addHeader("Primer-SDK-Version", "1.0.0-beta.0")
      .addHeader("Primer-SDK-Client", "ANDROID_NATIVE")
      .addHeader("Primer-Client-Token", clientToken.accessToken)
      .build()

    client.newCall(request).enqueue(object: Callback {
      override fun onFailure(call: Call, e: IOException) {
        handler.post {
          onError(APIErrorResponse.create(e))
        }
      }

      override fun onResponse(call: Call, response: Response) {
        handler.post {
          if (response.code != 200) {
            onError(APIErrorResponse.create(response))
          } else {
            callback(APISuccessResponse.create(response))
          }
        }
      }
    })
  }

  private fun toRequestBody(json: JSONObject?): RequestBody {
    val stringified = if (json == null) "{}" else json.toString();
    val mediaType = "application/json".toMediaType()
    return stringified.toRequestBody(mediaType)
  }
}