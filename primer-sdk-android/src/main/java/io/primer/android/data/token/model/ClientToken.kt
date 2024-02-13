package io.primer.android.data.token.model

import android.util.Base64
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.sequence
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
    val backendCallbackUrl: String?,
    val primerTransactionId: String?,
    val iPay88PaymentMethodId: String?,
    val iPay88ActionType: String?,
    val supportedCurrencyCode: String?,
    val supportedCountry: String?,
    val supportedThreeDsProtocolVersions: List<String>?,
    val nolPayTransactionNo: String?
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
                    val token = JSONSerializationUtils.getJsonObjectDeserializer<ClientToken>()
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
        private const val BACKEND_CALLBACK_URL_FIELD = "backendCallbackUrl"
        private const val PRIMER_TRANSACTION_ID_FIELD = "primerTransactionId"
        private const val IPAY88_PAYMENT_METHOD_ID_FIELD = "iPay88PaymentMethodId"
        private const val IPAY88_ACTION_TYPE_FIELD = "iPay88ActionType"
        private const val SUPPORTED_CURRENCY_CODE_FIELD = "supportedCurrencyCode"
        private const val SUPPORTED_COUNTRY_FIELD = "supportedCountry"
        private const val SUPPORTED_THREE_DS_PROTOCOL_VERSIONS_FIELD =
            "supportedThreeDsProtocolVersions"
        private const val NOL_PAY_TRANSACTION_NO = "nolPayTransactionNo"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            ClientToken(
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
                t.optNullableString(BACKEND_CALLBACK_URL_FIELD),
                t.optNullableString(PRIMER_TRANSACTION_ID_FIELD),
                t.optNullableString(IPAY88_PAYMENT_METHOD_ID_FIELD),
                t.optNullableString(IPAY88_ACTION_TYPE_FIELD),
                t.optNullableString(SUPPORTED_CURRENCY_CODE_FIELD),
                t.optNullableString(SUPPORTED_COUNTRY_FIELD),
                t.optJSONArray(SUPPORTED_THREE_DS_PROTOCOL_VERSIONS_FIELD)?.sequence<String>()
                    ?.toList(),
                t.optNullableString(NOL_PAY_TRANSACTION_NO)
            )
        }
    }
}
