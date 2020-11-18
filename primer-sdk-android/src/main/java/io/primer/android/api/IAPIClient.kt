package io.primer.android.api

import org.json.JSONObject

interface IAPIClient {
  fun get(
    url: String,
    callback: ((APISuccessResponse) -> Unit),
    onError: ((APIErrorResponse) -> Unit)
  )
  fun post(
    url: String,
    body: JSONObject?,
    callback: ((APISuccessResponse) -> Unit),
    onError: ((APIErrorResponse) -> Unit)
  )
}