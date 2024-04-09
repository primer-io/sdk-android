package io.primer.android.data.tokenization.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.extensions.optNullableInt
import io.primer.android.core.serialization.json.extensions.optNullableString
import org.json.JSONObject

data class PaymentInstrumentData(
    val network: String? = null,
    val cardholderName: String? = null,
    val first6Digits: Int? = null,
    val last4Digits: Int? = null,
    val expirationMonth: Int? = null,
    val expirationYear: Int? = null,
    val gocardlessMandateId: String? = null,
    val externalPayerInfo: ExternalPayerInfo? = null,
    val klarnaCustomerToken: String? = null,
    val sessionData: SessionData? = null,
    // apaya
    val mx: String? = null,
    val mnc: Int? = null,
    val mcc: Int? = null,
    val hashedIdentifier: String? = null,
    val currencyCode: String? = null,
    val productId: String? = null,
    // async
    val paymentMethodType: String? = null,
    // bin
    val binData: BinData? = null
) : JSONDeserializable {

    companion object {
        private const val NETWORK_FIELD = "network"
        private const val CARDHOLDER_NAME_FIELD = "cardholderName"
        private const val FIRST_6_DIGITS_FIELD = "first6Digits"
        private const val LAST_4_DIGITS_FIELD = "last4Digits"
        private const val EXPIRATION_MONTH_FIELD = "expirationMonth"
        private const val EXPIRATION_YEAR_FIELD = "expirationYear"
        private const val GO_CARDLESS_MANDATE_ID_FIELD = "gocardlessMandateId"
        private const val EXTERNAL_PAYER_INFO_FIELD = "externalPayerInfo"
        private const val KLARNA_CUSTOMER_TOKEN_FIELD = "klarnaCustomerToken"
        private const val SESSION_DATA_FIELD = "sessionData"
        private const val MX_FIELD = "mx"
        private const val MNC_FIELD = "mnc"
        private const val MCC_FIELD = "mcc"
        private const val HASHED_IDENTIFIER_FIELD = "hashedIdentifier"
        private const val CURRENCY_CODE_FIELD = "currencyCode"
        private const val PRODUCT_ID_FIELD = "productId"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val BIN_DATA_FIELD = "binData"

        @JvmField
        internal val deserializer = JSONObjectDeserializer { t ->
            PaymentInstrumentData(
                t.optNullableString(NETWORK_FIELD),
                t.optNullableString(CARDHOLDER_NAME_FIELD),
                t.optNullableInt(FIRST_6_DIGITS_FIELD),
                t.optNullableInt(LAST_4_DIGITS_FIELD),
                t.optNullableInt(EXPIRATION_MONTH_FIELD),
                t.optNullableInt(EXPIRATION_YEAR_FIELD),
                t.optNullableString(GO_CARDLESS_MANDATE_ID_FIELD),
                t.optJSONObject(EXTERNAL_PAYER_INFO_FIELD)?.let {
                    JSONSerializationUtils.getJsonObjectDeserializer<ExternalPayerInfo>()
                        .deserialize(it)
                },
                t.optNullableString(KLARNA_CUSTOMER_TOKEN_FIELD),
                t.optJSONObject(SESSION_DATA_FIELD)?.let {
                    JSONSerializationUtils.getJsonObjectDeserializer<SessionData>()
                        .deserialize(it)
                },
                t.optNullableString(MX_FIELD),
                t.optNullableInt(MNC_FIELD),
                t.optNullableInt(MCC_FIELD),
                t.optNullableString(HASHED_IDENTIFIER_FIELD),
                t.optNullableString(CURRENCY_CODE_FIELD),
                t.optNullableString(PRODUCT_ID_FIELD),
                t.optNullableString(PAYMENT_METHOD_TYPE_FIELD),
                t.optJSONObject(BIN_DATA_FIELD)?.let {
                    JSONSerializationUtils.getJsonObjectDeserializer<BinData>()
                        .deserialize(it)
                }
            )
        }
    }
}

data class ExternalPayerInfo(
    val email: String,
    val externalPayerId: String?,
    val firstName: String?,
    val lastName: String?
) : JSONDeserializable {
    companion object {
        private const val EMAIL_FIELD = "email"
        private const val EXTERNAL_PAYER_ID_FIELD = "externalPayerId"
        private const val FIRST_NAME_FIELD = "firstName"
        private const val LAST_NAME_FIELD = "lastName"

        @JvmField
        internal val deserializer = JSONObjectDeserializer { t ->
            ExternalPayerInfo(
                email = t.getString(EMAIL_FIELD),
                externalPayerId = t.optNullableString(EXTERNAL_PAYER_ID_FIELD),
                firstName = t.optNullableString(FIRST_NAME_FIELD),
                lastName = t.optNullableString(LAST_NAME_FIELD)
            )
        }
    }
}

data class SessionData(
    val recurringDescription: String? = null,
    val billingAddress: BillingAddress? = null
) : JSONDeserializable {
    companion object {
        private const val RECURRING_DESCRIPTION_FIELD = "recurringDescription"
        private const val BILLING_ADDRESS_FIELD = "billingAddress"

        @JvmField
        internal val deserializer = JSONObjectDeserializer { t ->
            SessionData(
                t.optNullableString(RECURRING_DESCRIPTION_FIELD),
                t.optJSONObject(BILLING_ADDRESS_FIELD)?.let {
                    JSONSerializationUtils.getJsonObjectDeserializer<BillingAddress>()
                        .deserialize(it)
                }
            )
        }
    }
}

data class BillingAddress(
    val email: String
) : JSONDeserializable {
    companion object {
        private const val EMAIL_FIELD = "email"

        @JvmField
        internal val deserializer = JSONObjectDeserializer { t ->
            BillingAddress(
                t.getString(EMAIL_FIELD)
            )
        }
    }
}

data class BinData(
    val network: String? = null
) : JSONObjectSerializable, JSONDeserializable {
    companion object {
        private const val NETWORK_FIELD = "network"

        @JvmField
        internal val serializer = JSONObjectSerializer<BinData> { t ->
            JSONObject().apply {
                putOpt(NETWORK_FIELD, t.network)
            }
        }

        @JvmField
        internal val deserializer =
            JSONObjectDeserializer { t -> BinData(t.optNullableString(NETWORK_FIELD)) }
    }
}

enum class TokenType {

    SINGLE_USE,
    MULTI_USE
}
