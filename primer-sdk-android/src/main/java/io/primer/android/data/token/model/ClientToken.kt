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
    val sdkCompleteUrl: String?,
    val stripePaymentIntentId: String?,
    val stripeCustomerId: String?,
    val stripeClientSecret: String?,
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
        private const val SDK_COMPLETE_URL_FIELD = "sdkCompleteUrl"
        private const val STRIPE_PAYMENT_INTENT_ID_FIELD = "stripePaymentIntentId"
        private const val STRIPE_CUSTOMER_ID_FIELD = "stripeCustomerId"
        private const val STRIPE_CLIENT_SECRET_FIELD = "stripeClientSecret"
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
                configurationUrl = t.optNullableString(CONFIGURATION_URL_FIELD),
                analyticsUrlV2 = t.optNullableString(ANALYTICS_URL_V2_FIELD),
                intent = t.getString(INTENT_FIELD),
                accessToken = t.getString(ACCESS_TOKEN_FIELD),
                exp = t.getInt(EXP_FIELD),
                statusUrl = t.optNullableString(STATUS_URL_FIELD),
                redirectUrl = t.optNullableString(REDIRECT_URL_FIELD),
                sdkCompleteUrl = t.optNullableString(SDK_COMPLETE_URL_FIELD),
                stripePaymentIntentId = t.optNullableString(STRIPE_PAYMENT_INTENT_ID_FIELD),
                stripeCustomerId = t.optNullableString(STRIPE_CUSTOMER_ID_FIELD),
                stripeClientSecret = t.optNullableString(STRIPE_CLIENT_SECRET_FIELD),
                qrCode = t.optNullableString(QR_CODE_FIELD),
                accountNumber = t.optNullableString(ACCOUNT_NUMBER_FIELD),
                expiration = t.optNullableString(EXPIRATION_FIELD),
                qrCodeUrl = t.optNullableString(QR_CODE_URL_FIELD),
                expiresAt = t.optNullableString(EXPIRES_AT_FIELD),
                reference = t.optNullableString(REFERENCE_FIELD),
                entity = t.optNullableString(ENTITY_FIELD),
                backendCallbackUrl = t.optNullableString(BACKEND_CALLBACK_URL_FIELD),
                primerTransactionId = t.optNullableString(PRIMER_TRANSACTION_ID_FIELD),
                iPay88PaymentMethodId = t.optNullableString(IPAY88_PAYMENT_METHOD_ID_FIELD),
                iPay88ActionType = t.optNullableString(IPAY88_ACTION_TYPE_FIELD),
                supportedCurrencyCode = t.optNullableString(SUPPORTED_CURRENCY_CODE_FIELD),
                supportedCountry = t.optNullableString(SUPPORTED_COUNTRY_FIELD),
                supportedThreeDsProtocolVersions =
                t.optJSONArray(SUPPORTED_THREE_DS_PROTOCOL_VERSIONS_FIELD)
                    ?.sequence<String>()
                    ?.toList(),
                nolPayTransactionNo = t.optNullableString(NOL_PAY_TRANSACTION_NO)
            )
        }
    }
}
