package io.primer.android.api

import com.android.volley.VolleyError
import io.primer.android.logging.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.lang.Exception
import java.util.*

data class APIErrorResponse(
  val statusCode: Int,
  val data: APIError
) {

  @Serializable
  data class APIError(
    val description: String,
    val errorId: String? = null,
    val diagnosticsId: String? = null,
    val validationErrors: List<ValidationError> = Collections.emptyList()
  )

  @Serializable
  data class ValidationErrorDetail(
    val path: String,
    val description: String
  )

  @Serializable
  data class ValidationError(
    val model: String,
    val errors: List<ValidationErrorDetail>
  )

  companion object {
    private val format = Json { ignoreUnknownKeys = true }
    private val log = Logger("api-error")

    fun create(e: VolleyError?): APIErrorResponse {
      val statusCode = e?.networkResponse?.statusCode ?: -1
      val bytes = e?.networkResponse?.data
      val serialized = if (bytes == null) null else String(bytes)
      val element = getErrorFromContent(serialized)

      return APIErrorResponse(
        statusCode = statusCode,
        format.decodeFromJsonElement(element)
      )
    }

    private fun getErrorFromContent(content: String?): JsonObject {
      val defaultElement = format.parseToJsonElement(
        "{\"description\":\"Unknown Client Error\"}"
      )

      if (content == null) {
        return defaultElement.jsonObject
      }

      try {
        val json = format.parseToJsonElement(content).jsonObject
        if (json.containsKey("error")) {
          return json["error"]!!.jsonObject
        }

        if (json.containsKey("message")) {
          val message = json["message"]?.jsonPrimitive?.content ?: "Unknown Client Error"
          val jsonString = "{\"description\": \"$message\"}"
          return format.parseToJsonElement(jsonString).jsonObject
        }
      } catch (ex: Exception) {
        log.warn("Failed to decode json response")
        log.warn(content)
      }

      return defaultElement.jsonObject
    }
  }
}