package io.primer.android.data.token.validation.model

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ValidationTokenDataRequest(
    val clientToken: String
) : JSONObjectSerializable {

    companion object {
        private const val CLIENT_TOKEN_FIELD = "clientToken"

        @JvmField
        val serializer = object : JSONObjectSerializer<ValidationTokenDataRequest> {
            override fun serialize(t: ValidationTokenDataRequest): JSONObject {
                return JSONObject().apply {
                    put(CLIENT_TOKEN_FIELD, t.clientToken)
                }
            }
        }
    }
}

internal fun String.toValidationTokenData() = ValidationTokenDataRequest(this)
