package io.primer.android.api

import org.json.JSONObject

data class APISuccessResponse(
  val statusCode: Int,
  val data: JSONObject
) {
  companion object {
    fun create(data: JSONObject): APISuccessResponse {
      return APISuccessResponse(
        statusCode = 200,
        data = data,
      )
    }
  }
}