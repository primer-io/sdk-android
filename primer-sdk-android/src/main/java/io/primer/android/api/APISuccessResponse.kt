package io.primer.android.api

import okhttp3.Response
import org.json.JSONObject

data class APISuccessResponse(
  val statusCode: Int,
  val data: JSONObject
) {
  companion object {
    fun create(response: Response): APISuccessResponse {
      val content = response.body?.string() ?: "{}"
      val data = JSONObject(content)

      return APISuccessResponse(
        statusCode = 200,
        data = data,
      )
    }
  }
}