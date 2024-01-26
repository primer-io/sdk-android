package io.primer.android.data.error.model

import io.primer.android.core.logging.internal.DefaultLogReporter
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
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
            val deserializer = object : JSONDeserializer<ValidationErrorDetail> {
                override fun deserialize(t: JSONObject): ValidationErrorDetail {
                    return ValidationErrorDetail(
                        t.getString(PATH_FIELD),
                        t.getString(DESCRIPTION_FIELD)
                    )
                }
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
            val deserializer = object : JSONDeserializer<ValidationError> {
                override fun deserialize(t: JSONObject): ValidationError {
                    return ValidationError(
                        t.getString(MODEL_FIELD),
                        t.getJSONArray(ERRORS_FIELD).sequence<JSONObject>().map {
                            JSONSerializationUtils.getDeserializer<ValidationErrorDetail>()
                                .deserialize(it)
                        }.toList()
                    )
                }
            }
        }
    }

    companion object {
        private val logReporter = DefaultLogReporter()

        private const val DEFAULT_ERROR_ELEMENT =
            """{
                    "error": {
                        "description": "Unknown Client Error."
                    }
                }"""

        fun create(response: Response): APIError {
            return JSONSerializationUtils.getDeserializer<APIError>()
                .deserialize(getErrorFromContent(response.body))
        }

        private fun getErrorFromContent(body: ResponseBody?): JSONObject {
            if (body == null) {
                return JSONObject(DEFAULT_ERROR_ELEMENT)
            }

            val content = body.string()

            try {
                return JSONObject(content).getJSONObject("error")
            } catch (ignored: Exception) {
                logReporter.warn("Failed to decode json response")
                logReporter.warn(content)
            }

            return JSONObject(DEFAULT_ERROR_ELEMENT)
        }

        private const val DESCRIPTION_FIELD = "description"
        private const val ERROR_ID_FIELD = "errorId"
        private const val DIAGNOSTICS_ID_FIELD = "diagnosticsId"
        private const val VALIDATION_ERRORS_FIELD = "validationErrors"

        @JvmField
        val deserializer = object : JSONDeserializer<APIError> {
            override fun deserialize(t: JSONObject): APIError {
                return APIError(
                    t.getString(DESCRIPTION_FIELD),
                    t.optNullableString(ERROR_ID_FIELD),
                    t.optNullableString(DIAGNOSTICS_ID_FIELD),
                    t.optJSONArray(VALIDATION_ERRORS_FIELD)?.sequence<JSONObject>()?.map {
                        JSONSerializationUtils.getDeserializer<ValidationError>()
                            .deserialize(it)
                    }?.toList().orEmpty()
                )
            }
        }
    }
}
