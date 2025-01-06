// package structure is kept in order to maintain backward compatibility
package io.primer.android.data.tokenization.models

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableInt
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import org.json.JSONObject

data class PaymentInstrumentData(
    val network: String? = null,
    val cardholderName: String? = null,
    val first6Digits: Int? = null,
    val last4Digits: Int? = null,
    val accountNumberLast4Digits: Int? = null,
    val expirationMonth: Int? = null,
    val expirationYear: Int? = null,
    val externalPayerInfo: ExternalPayerInfo? = null,
    val klarnaCustomerToken: String? = null,
    val sessionData: SessionData? = null,
    // async
    val paymentMethodType: String? = null,
    val sessionInfo: SessionInfo? = null,
    // bin
    val binData: BinData? = null,
    val bankName: String? = null,
) : JSONDeserializable {
    companion object {
        private const val NETWORK_FIELD = "network"
        private const val CARDHOLDER_NAME_FIELD = "cardholderName"
        private const val FIRST_6_DIGITS_FIELD = "first6Digits"
        private const val LAST_4_DIGITS_FIELD = "last4Digits"
        private const val ACCOUNT_NUMBER_LAST_4_DIGITS_FIELD = "accountNumberLastFourDigits"
        private const val EXPIRATION_MONTH_FIELD = "expirationMonth"
        private const val EXPIRATION_YEAR_FIELD = "expirationYear"
        private const val EXTERNAL_PAYER_INFO_FIELD = "externalPayerInfo"
        private const val KLARNA_CUSTOMER_TOKEN_FIELD = "klarnaCustomerToken"
        private const val SESSION_DATA_FIELD = "sessionData"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val SESSION_INFO_FIELD = "sessionInfo"
        private const val BIN_DATA_FIELD = "binData"
        private const val BANK_NAME_FIELD = "bankName"

        @JvmField
        internal val deserializer =
            JSONObjectDeserializer { t ->
                PaymentInstrumentData(
                    network = t.optNullableString(NETWORK_FIELD),
                    cardholderName = t.optNullableString(CARDHOLDER_NAME_FIELD),
                    first6Digits = t.optNullableInt(FIRST_6_DIGITS_FIELD),
                    last4Digits = t.optNullableInt(LAST_4_DIGITS_FIELD),
                    accountNumberLast4Digits = t.optNullableInt(ACCOUNT_NUMBER_LAST_4_DIGITS_FIELD),
                    expirationMonth = t.optNullableInt(EXPIRATION_MONTH_FIELD),
                    expirationYear = t.optNullableInt(EXPIRATION_YEAR_FIELD),
                    externalPayerInfo =
                        t.optJSONObject(EXTERNAL_PAYER_INFO_FIELD)?.let {
                            JSONSerializationUtils.getJsonObjectDeserializer<ExternalPayerInfo>()
                                .deserialize(it)
                        },
                    klarnaCustomerToken = t.optNullableString(KLARNA_CUSTOMER_TOKEN_FIELD),
                    sessionData =
                        t.optJSONObject(SESSION_DATA_FIELD)?.let {
                            JSONSerializationUtils.getJsonObjectDeserializer<SessionData>()
                                .deserialize(it)
                        },
                    paymentMethodType = t.optNullableString(PAYMENT_METHOD_TYPE_FIELD),
                    t.optJSONObject(SESSION_INFO_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<SessionInfo>()
                            .deserialize(it)
                    },
                    binData =
                        t.optJSONObject(BIN_DATA_FIELD)?.let {
                            JSONSerializationUtils.getJsonObjectDeserializer<BinData>()
                                .deserialize(it)
                        },
                    bankName = t.optNullableString(BANK_NAME_FIELD),
                )
            }
    }
}

data class ExternalPayerInfo(
    val email: String,
    val externalPayerId: String?,
    val firstName: String?,
    val lastName: String?,
) : JSONDeserializable {
    companion object {
        private const val EMAIL_FIELD = "email"
        private const val EXTERNAL_PAYER_ID_FIELD = "externalPayerId"
        private const val FIRST_NAME_FIELD = "firstName"
        private const val LAST_NAME_FIELD = "lastName"

        @JvmField
        internal val deserializer =
            JSONObjectDeserializer { t ->
                ExternalPayerInfo(
                    email = t.getString(EMAIL_FIELD),
                    externalPayerId = t.optNullableString(EXTERNAL_PAYER_ID_FIELD),
                    firstName = t.optNullableString(FIRST_NAME_FIELD),
                    lastName = t.optNullableString(LAST_NAME_FIELD),
                )
            }
    }
}

data class SessionData(
    val recurringDescription: String? = null,
    val billingAddress: BillingAddress? = null,
) : JSONDeserializable {
    companion object {
        private const val RECURRING_DESCRIPTION_FIELD = "recurringDescription"
        private const val BILLING_ADDRESS_FIELD = "billingAddress"

        @JvmField
        internal val deserializer =
            JSONObjectDeserializer { t ->
                SessionData(
                    t.optNullableString(RECURRING_DESCRIPTION_FIELD),
                    t.optJSONObject(BILLING_ADDRESS_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<BillingAddress>()
                            .deserialize(it)
                    },
                )
            }
    }
}

data class BillingAddress(
    val email: String,
) : JSONDeserializable {
    companion object {
        private const val EMAIL_FIELD = "email"

        @JvmField
        internal val deserializer =
            JSONObjectDeserializer { t ->
                BillingAddress(
                    t.getString(EMAIL_FIELD),
                )
            }
    }
}

data class SessionInfo(
    val locale: String?,
    val platform: String?,
    val retailOutlet: String?,
) : JSONDeserializable {
    companion object {
        private const val LOCALE_FIELD = "locale"
        private const val PLATFORM_FIELD = "platform"
        private const val RETAIL_OUTLET_FIELD = "retailOutlet"

        @JvmField
        internal val deserializer =
            JSONObjectDeserializer { t ->
                SessionInfo(
                    locale = t.optNullableString(LOCALE_FIELD),
                    platform = t.optNullableString(PLATFORM_FIELD),
                    retailOutlet = t.optNullableString(RETAIL_OUTLET_FIELD),
                )
            }
    }
}

data class BinData(
    val network: String? = null,
) : JSONObjectSerializable, JSONDeserializable {
    companion object {
        private const val NETWORK_FIELD = "network"

        @JvmField
        internal val serializer =
            JSONObjectSerializer<BinData> { t ->
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
    MULTI_USE,
}
