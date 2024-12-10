package io.primer.android.banks.implementation.payment.resume.clientToken.data.model

import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject

internal data class BankIssuerClientTokenData(
    val intent: String,
    val statusUrl: String,
    val redirectUrl: String
) : JSONDeserializable {

    companion object {

        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): BankIssuerClientTokenData {
            ClientTokenDecoder.decode(encoded).let { decoded ->
                return JSONSerializationUtils
                    .getJsonObjectDeserializer<BankIssuerClientTokenData>()
                    .deserialize(JSONObject(decoded))
            }
        }

        private const val INTENT_FIELD = "intent"
        private const val STATUS_URL_FIELD = "statusUrl"
        private const val REDIRECT_URL_FIELD = "redirectUrl"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            BankIssuerClientTokenData(
                t.getString(INTENT_FIELD),
                t.getString(STATUS_URL_FIELD),
                t.getString(REDIRECT_URL_FIELD)
            )
        }
    }
}
