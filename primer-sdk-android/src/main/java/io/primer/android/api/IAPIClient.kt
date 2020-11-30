package io.primer.android.api

import org.json.JSONObject

interface IAPIClient {
  fun get(url: String): Observable
  fun post(url: String, body: JSONObject?): Observable
}