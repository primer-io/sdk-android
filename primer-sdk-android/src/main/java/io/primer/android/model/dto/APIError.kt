package io.primer.android.model.dto

import io.primer.android.logging.Logger
import io.primer.android.model.Serialization
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import java.util.Collections

@Serializable
data class APIError(
    val description: String,
    val errorId: String? = null,
    val diagnosticsId: String? = null,
    val validationErrors: List<ValidationError> = Collections.emptyList(),
) {

    @Serializable
    data class ValidationErrorDetail(
        val path: String,
        val description: String,
    )

    @Serializable
    data class ValidationError(
        val model: String,
        val errors: List<ValidationErrorDetail>,
    )

    companion object {

        private val json = Serialization.json

        private val log = Logger("api-error")

        private val DEFAULT_ERROR_ELEMENT = json.parseToJsonElement(
            "{\"description\":\"Unknown Client Error\"}"
        )

        fun create(response: Response): APIError {
            return json.decodeFromJsonElement(getErrorFromContent(response.body()))
        }

        fun create(ignored: IOException?): APIError {
            return json.decodeFromJsonElement(DEFAULT_ERROR_ELEMENT)
        }

        fun createDefault(): APIError {
            return json.decodeFromJsonElement(DEFAULT_ERROR_ELEMENT)
        }

        fun createDefaultWithMessage(message: String): APIError {
            return APIError(message)
        }

        private fun getErrorFromContent(body: ResponseBody?): JsonObject {
            if (body == null) {
                return DEFAULT_ERROR_ELEMENT.jsonObject
            }

            val content = body.string()

            try {
                val element = json.parseToJsonElement(content).jsonObject
                if (element.containsKey("error")) {
                    return element["error"]!!.jsonObject
                }

                if (element.containsKey("message")) {
                    val message =
                        element["message"]?.jsonPrimitive?.content ?: "Unknown Client Error"
                    val jsonString = "{\"description\": \"$message\"}"
                    return json.parseToJsonElement(jsonString).jsonObject
                }
            } catch (ignored: Exception) {
                log.warn("Failed to decode json response")
                log.warn(content)
            }

            return DEFAULT_ERROR_ELEMENT.jsonObject
        }
    }
}
