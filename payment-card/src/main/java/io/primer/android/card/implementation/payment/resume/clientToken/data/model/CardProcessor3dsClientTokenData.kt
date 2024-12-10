package io.primer.android.card.implementation.payment.resume.clientToken.data.model

import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject

internal data class CardProcessor3dsClientTokenData(
    val intent: String,
    val statusUrl: String,
    val redirectUrl: String
) : JSONDeserializable {

    companion object {

        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): CardProcessor3dsClientTokenData {
            ClientTokenDecoder.decode(encoded).let { decoded ->
                return JSONSerializationUtils
                    .getJsonObjectDeserializer<CardProcessor3dsClientTokenData>()
                    .deserialize(JSONObject(decoded))
            }
        }

        private const val INTENT_FIELD = "intent"
        private const val STATUS_URL_KEY = "statusUrl"
        private const val REDIRECT_URL_FIELD = "redirectUrl"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            CardProcessor3dsClientTokenData(
                intent = t.getString(INTENT_FIELD),
                statusUrl = t.getString(STATUS_URL_KEY),
                redirectUrl = t.getString(REDIRECT_URL_FIELD)
            )
        }
    }
}
