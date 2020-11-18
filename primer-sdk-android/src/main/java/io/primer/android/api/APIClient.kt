package io.primer.android.api

import io.primer.android.session.ClientToken

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.Volley
import io.primer.android.logging.Logger
import org.json.JSONObject


class APIClient(context: Context, token: ClientToken) : IAPIClient {
  private var log = Logger("api-client")
  private var queue = Volley.newRequestQueue(context)
  private var clientToken = token

  override fun get(
    url: String,
    callback: ((APISuccessResponse) -> Unit),
    onError: ((APIErrorResponse) -> Unit)
  ) {
    this.request(Request.Method.GET, url, null, callback, onError)
  }

  override fun post(
    url: String,
    body: JSONObject?,
    callback: ((APISuccessResponse) -> Unit),
    onError: ((APIErrorResponse) -> Unit)
  ) {
    this.request(Request.Method.GET, url, body, callback, onError)
  }

  private fun request(
    method: Int,
    url: String,
    body: JSONObject?,
    callback: ((APISuccessResponse) -> Unit),
    onError: ((APIErrorResponse) -> Unit)
  ) {
    log("Making request to: $url : $method")

    val request = APIRequest(
      clientToken,
      method,
      url,
      body,
      {
          data -> callback(APISuccessResponse.create(data))
      },
      {
          error -> onError(APIErrorResponse.create(error))
      }
    )

    queue.add(request)
  }
}