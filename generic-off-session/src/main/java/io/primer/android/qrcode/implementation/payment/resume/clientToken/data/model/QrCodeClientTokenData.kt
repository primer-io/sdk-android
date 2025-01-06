package io.primer.android.qrcode.implementation.payment.resume.clientToken.data.model

import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import org.json.JSONObject

internal data class QrCodeClientTokenData(
    val intent: String,
    val statusUrl: String,
    val expiresAt: String?,
    val qrCodeUrl: String?,
    val qrCodeBase64: String,
) : JSONDeserializable {
    companion object {
        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): QrCodeClientTokenData {
            ClientTokenDecoder.decode(encoded).let { decoded ->
                return JSONSerializationUtils
                    .getJsonObjectDeserializer<QrCodeClientTokenData>()
                    .deserialize(JSONObject(decoded))
            }
        }

        private const val INTENT_FIELD = "intent"
        private const val STATUS_URL_FIELD = "statusUrl"
        private const val EXPIRES_AT_FIELD = "expiresAt"
        private const val QR_CODE_FIELD = "qrCode"
        private const val QR_CODE_URL_FIELD = "qrCodeUrl"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                QrCodeClientTokenData(
                    intent = t.getString(INTENT_FIELD),
                    statusUrl = t.getString(STATUS_URL_FIELD),
                    expiresAt = t.optNullableString(EXPIRES_AT_FIELD),
                    qrCodeUrl = t.optNullableString(QR_CODE_URL_FIELD),
                    qrCodeBase64 = t.getString(QR_CODE_FIELD),
                )
            }
    }
}
