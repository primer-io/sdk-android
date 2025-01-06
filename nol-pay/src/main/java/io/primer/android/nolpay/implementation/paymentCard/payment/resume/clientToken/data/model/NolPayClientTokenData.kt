package io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.data.model

import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject

internal data class NolPayClientTokenData(
    val intent: String,
    val transactionNumber: String,
    val statusUrl: String,
    val completeUrl: String,
) : JSONDeserializable {
    companion object {
        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): NolPayClientTokenData {
            ClientTokenDecoder.decode(encoded).let { decoded ->
                return JSONSerializationUtils
                    .getJsonObjectDeserializer<NolPayClientTokenData>()
                    .deserialize(JSONObject(decoded))
            }
        }

        private const val INTENT_FIELD = "intent"
        private const val STATUS_URL_FIELD = "statusUrl"
        private const val COMPLETE_URL_FIELD = "redirectUrl"
        private const val TRANSACTION_NO_FIELD = "nolPayTransactionNo"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                NolPayClientTokenData(
                    intent = t.getString(INTENT_FIELD),
                    transactionNumber = t.getString(TRANSACTION_NO_FIELD),
                    statusUrl = t.getString(STATUS_URL_FIELD),
                    completeUrl = t.getString(COMPLETE_URL_FIELD),
                )
            }
    }
}
