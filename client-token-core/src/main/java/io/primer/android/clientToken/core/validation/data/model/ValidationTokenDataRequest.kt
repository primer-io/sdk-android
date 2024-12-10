package io.primer.android.clientToken.core.validation.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ValidationTokenDataRequest(
    val clientToken: String
) : JSONObjectSerializable {

    companion object {
        private const val CLIENT_TOKEN_FIELD = "clientToken"

        @JvmField
        val serializer = JSONObjectSerializer<ValidationTokenDataRequest> { t ->
            JSONObject().apply {
                put(CLIENT_TOKEN_FIELD, t.clientToken)
            }
        }
    }
}

internal fun String.toValidationTokenData() = ValidationTokenDataRequest(this)
