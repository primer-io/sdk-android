package io.primer.android.api

import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import io.primer.android.session.ClientToken
import org.json.JSONObject


class APIRequest : JsonObjectRequest {
  private var accessToken: String

  constructor(
    clientToken: ClientToken,
    method: Int,
    url: String,
    body: JSONObject?,
    listener: Response.Listener<JSONObject>,
    errorListener: Response.ErrorListener
  ) : super(method, url, body, listener, errorListener) {
    accessToken = clientToken.accessToken
  }

  override fun getHeaders(): MutableMap<String, String> {
    val map = HashMap<String, String>()
    map["Content-Type"] = "application/json"
    map["Primer-SDK-Version"] = "1.0.0"
    map["Primer-SDK-Client"] = "ANDROID_NATIVE"
    map["Primer-Client-Token"] = accessToken
    return map
  }
}