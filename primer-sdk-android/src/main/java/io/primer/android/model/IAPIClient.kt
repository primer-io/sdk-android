package io.primer.android.model

import org.json.JSONObject

internal interface IAPIClient {
  fun get(url: String): Observable
  fun post(url: String, body: JSONObject?): Observable
}