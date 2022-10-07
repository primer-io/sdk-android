package io.primer.android.data.token.model

import android.util.Base64
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.data.token.exception.ExpiredClientTokenException
import io.primer.android.data.token.exception.InvalidClientTokenException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

internal data class ClientToken(
    val configurationUrl: String?,
    val analyticsUrlV2: String?,
    val intent: String,
    val accessToken: String,
    val exp: Int,
    val statusUrl: String?,
    val redirectUrl: String?,
    val qrCode: String?,
    val accountNumber: String?,
    val expiration: String?,
    val qrCodeUrl: String?,
    val expiresAt: String?,
    val reference: String?,
    val entity: String?,
) : JSONDeserializable {

    companion object {
        private fun checkIfExpired(expInSeconds: Long): Boolean {
            return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) >= expInSeconds
        }

        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): ClientToken {
            if (encoded.isBlank()) throw InvalidClientTokenException()

            val tokens = encoded.split(".")

            for (elm in tokens) {
                val bytes = Base64.decode(elm, Base64.URL_SAFE)
                val decoded = String(bytes)

                if (decoded.contains("\"accessToken\":")) {
                    val token = JSONSerializationUtils.getDeserializer<ClientToken>()
                        .deserialize(JSONObject(decoded))

                    val isExpired = checkIfExpired(token.exp.toLong())

                    if (isExpired) throw ExpiredClientTokenException()

                    return token
                }
            }

            throw InvalidClientTokenException()
        }

        private const val CONFIGURATION_URL_FIELD = "configurationUrl"
        private const val ANALYTICS_URL_V2_FIELD = "analyticsUrlV2"
        private const val INTENT_FIELD = "intent"
        private const val ACCESS_TOKEN_FIELD = "accessToken"
        private const val EXP_FIELD = "exp"
        private const val STATUS_URL_FIELD = "statusUrl"
        private const val REDIRECT_URL_FIELD = "redirectUrl"
        private const val QR_CODE_FIELD = "qrCode"
        private const val ACCOUNT_NUMBER_FIELD = "accountNumber"
        private const val EXPIRATION_FIELD = "expiration"
        private const val QR_CODE_URL_FIELD = "qrCodeUrl"
        private const val EXPIRES_AT_FIELD = "expiresAt"
        private const val REFERENCE_FIELD = "reference"
        private const val ENTITY_FIELD = "entity"

        @JvmField
        val deserializer = object : JSONDeserializer<ClientToken> {
            override fun deserialize(t: JSONObject): ClientToken {
                return ClientToken(
                    t.optNullableString(CONFIGURATION_URL_FIELD),
                    t.optNullableString(ANALYTICS_URL_V2_FIELD),
                    t.getString(INTENT_FIELD),
                    t.getString(ACCESS_TOKEN_FIELD),
                    t.getInt(EXP_FIELD),
                    t.optNullableString(STATUS_URL_FIELD),
                    t.optNullableString(REDIRECT_URL_FIELD),
                    t.optNullableString(QR_CODE_FIELD),
                    t.optNullableString(ACCOUNT_NUMBER_FIELD),
                    t.optNullableString(EXPIRATION_FIELD),
                    t.optNullableString(QR_CODE_URL_FIELD),
                    t.optNullableString(EXPIRES_AT_FIELD),
                    t.optNullableString(REFERENCE_FIELD),
                    t.optNullableString(ENTITY_FIELD),
                )
            }
        }
    }
}
