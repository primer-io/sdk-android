package io.primer.android.phoneNumber.implementation.payment.resume.clientToken.data.model

import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject

internal data class PhoneNumberClientTokenData(
    val intent: String,
    val statusUrl: String,
) : JSONDeserializable {
    companion object {
        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): PhoneNumberClientTokenData {
            ClientTokenDecoder.decode(encoded).let { decoded ->
                return JSONSerializationUtils
                    .getJsonObjectDeserializer<PhoneNumberClientTokenData>()
                    .deserialize(JSONObject(decoded))
            }
        }

        private const val INTENT_FIELD = "intent"
        private const val STATUS_URL_FIELD = "statusUrl"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                PhoneNumberClientTokenData(
                    intent = t.getString(INTENT_FIELD),
                    statusUrl = t.getString(STATUS_URL_FIELD),
                )
            }
    }
}
