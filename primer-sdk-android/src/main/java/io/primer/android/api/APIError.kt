package io.primer.android.api

import io.primer.android.logging.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import java.lang.Exception
import java.util.*

@Serializable
data class APIError(
  val description: String,
  val errorId: String? = null,
  val diagnosticsId: String? = null,
  val validationErrors: List<ValidationError> = Collections.emptyList()
) {
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
    private val DEFAULT_ERROR_ELEMENT = format.parseToJsonElement(
      "{\"description\":\"Unknown Client Error\"}"
    )

    fun create(response: Response): APIError {
      return format.decodeFromJsonElement(getErrorFromContent(response.body))
    }

    fun create(e: IOException?): APIError {
      return format.decodeFromJsonElement(DEFAULT_ERROR_ELEMENT)
    }

    private fun getErrorFromContent(body: ResponseBody?): JsonObject {
      if (body == null) {
        return DEFAULT_ERROR_ELEMENT.jsonObject
      }

      val content = body.string()

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

      return DEFAULT_ERROR_ELEMENT.jsonObject
    }
  }
}
