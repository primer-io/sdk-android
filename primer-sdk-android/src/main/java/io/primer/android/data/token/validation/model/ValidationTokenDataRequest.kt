package io.primer.android.data.token.validation.model

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject

internal data class ValidationTokenDataRequest(
    val clientToken: String
) : JSONSerializable {

    companion object {
        private const val CLIENT_TOKEN_FIELD = "clientToken"

        @JvmField
        val serializer = object : JSONSerializer<ValidationTokenDataRequest> {
            override fun serialize(t: ValidationTokenDataRequest): JSONObject {
                return JSONObject().apply {
                    put(CLIENT_TOKEN_FIELD, t.clientToken)
                }
            }
        }
    }
}

internal fun String.toValidationTokenData() = ValidationTokenDataRequest(this)
