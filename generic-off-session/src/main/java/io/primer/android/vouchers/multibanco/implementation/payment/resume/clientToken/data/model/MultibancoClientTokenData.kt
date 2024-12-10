package io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.data.model

import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject

internal data class MultibancoClientTokenData(
    val intent: String,
    val expiresAt: String,
    val reference: String,
    val entity: String
) : JSONDeserializable {

    companion object {

        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): MultibancoClientTokenData {
            ClientTokenDecoder.decode(encoded).let { decoded ->
                return JSONSerializationUtils
                    .getJsonObjectDeserializer<MultibancoClientTokenData>()
                    .deserialize(JSONObject(decoded))
            }
        }

        private const val INTENT_FIELD = "intent"
        private const val EXPIRES_AT_FIELD = "expiresAt"
        private const val REFERENCE_FIELD = "reference"
        private const val ENTITY_FIELD = "entity"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            MultibancoClientTokenData(
                intent = t.getString(INTENT_FIELD),
                expiresAt = t.getString(EXPIRES_AT_FIELD),
                entity = t.getString(ENTITY_FIELD),
                reference = t.getString(REFERENCE_FIELD)
            )
        }
    }
}
