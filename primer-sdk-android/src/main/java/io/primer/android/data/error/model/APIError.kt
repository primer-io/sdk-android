package io.primer.android.data.error.model

import io.primer.android.core.logging.internal.DefaultLogReporter
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.sequence
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject

internal data class APIError(
    val description: String,
    val errorId: String?,
    val diagnosticsId: String?,
    val validationErrors: List<ValidationError>
) : JSONDeserializable {

    data class ValidationErrorDetail(
        val path: String,
        val description: String
    ) : JSONDeserializable {
        companion object {

            private const val PATH_FIELD = "path"
            private const val DESCRIPTION_FIELD = "description"

            @JvmField
            val deserializer = JSONObjectDeserializer { t ->
                ValidationErrorDetail(
                    t.getString(PATH_FIELD),
                    t.getString(DESCRIPTION_FIELD)
                )
            }
        }
    }

    data class ValidationError(
        val model: String,
        val errors: List<ValidationErrorDetail>
    ) : JSONDeserializable {
        companion object {

            private const val MODEL_FIELD = "model"
            private const val ERRORS_FIELD = "errors"

            @JvmField
            val deserializer = JSONObjectDeserializer<ValidationError> { t ->
                ValidationError(
                    t.getString(MODEL_FIELD),
                    t.getJSONArray(ERRORS_FIELD).sequence<JSONObject>().map {
                        JSONSerializationUtils
                            .getJsonObjectDeserializer<ValidationErrorDetail>()
                            .deserialize(it)
                    }.toList()
                )
            }
        }
    }

    companion object {
        private val logReporter = DefaultLogReporter()

        private const val DEFAULT_ERROR_ELEMENT =
            """{
                "description":"Failed to decode json response."
            }"""

        fun create(response: Response): APIError {
            return JSONSerializationUtils.getJsonObjectDeserializer<APIError>()
                .deserialize(getErrorFromContent(response.body))
        }

        private fun getErrorFromContent(body: ResponseBody?): JSONObject {
            if (body == null) {
                return JSONObject(DEFAULT_ERROR_ELEMENT)
            }

            val content = body.string()

            return try {
                JSONObject(content).optJSONObject("error") ?: JSONObject(content)
            } catch (ignored: Exception) {
                logReporter.warn("Failed to decode json response: $content")
                JSONObject(DEFAULT_ERROR_ELEMENT)
            }
        }

        private const val DESCRIPTION_FIELD = "description"
        private const val DETAIL_FIELD = "detail"
        private const val ERROR_ID_FIELD = "errorId"
        private const val DIAGNOSTICS_ID_FIELD = "diagnosticsId"
        private const val VALIDATION_ERRORS_FIELD = "validationErrors"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            APIError(
                t.optNullableString(DESCRIPTION_FIELD)
                    ?: t.optNullableString(DETAIL_FIELD).orEmpty(),
                t.optNullableString(ERROR_ID_FIELD),
                t.optNullableString(DIAGNOSTICS_ID_FIELD),
                t.optJSONArray(VALIDATION_ERRORS_FIELD)?.sequence<JSONObject>()?.map {
                    JSONSerializationUtils.getJsonObjectDeserializer<ValidationError>()
                        .deserialize(it)
                }?.toList().orEmpty()
            )
        }
    }
}
