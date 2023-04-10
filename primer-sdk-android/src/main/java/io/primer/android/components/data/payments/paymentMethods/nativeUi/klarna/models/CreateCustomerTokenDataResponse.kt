package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.extensions.optNullableInt
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.sequence
import org.json.JSONArray
import org.json.JSONObject

internal data class CreateCustomerTokenDataResponse(
    val customerTokenId: String?,
    val sessionData: SessionData
) : JSONDeserializable {

    data class SessionData(
        val recurringDescription: String?,
        val purchaseCountry: String?,
        val purchaseCurrency: String?,
        val locale: String?,
        val orderAmount: Int?,
        val orderLines: List<SessionOrderLines>,
        val billingAddress: BillingAddressData?,
        val tokenDetails: TokenDetailsData?
    ) : JSONObjectSerializable, JSONDeserializable {

        companion object {
            private const val RECURRING_DESCRIPTION_FIELD = "recurringDescription"
            private const val PURCHASE_COUNTRY_FIELD = "purchaseCountry"
            private const val PURCHASE_CURRENCY_FIELD = "purchaseCurrency"
            private const val LOCALE_FIELD = "locale"
            private const val ORDER_AMOUNT_FIELD = "orderAmount"
            private const val ORDER_LINES_FIELD = "orderLines"
            private const val BILLING_ADDRESS_FIELD = "billingAddress"
            private const val TOKEN_DETAILS_FIELD = "tokenDetails"

            @JvmField
            val serializer = object : JSONObjectSerializer<SessionData> {

                override fun serialize(t: SessionData): JSONObject {
                    return JSONObject().apply {
                        putOpt(RECURRING_DESCRIPTION_FIELD, t.recurringDescription)
                        putOpt(PURCHASE_COUNTRY_FIELD, t.purchaseCountry)
                        putOpt(PURCHASE_CURRENCY_FIELD, t.purchaseCurrency)
                        putOpt(LOCALE_FIELD, t.locale)
                        putOpt(ORDER_AMOUNT_FIELD, t.orderAmount)
                        put(
                            ORDER_LINES_FIELD,
                            JSONArray().apply {
                                t.orderLines.map {
                                    put(
                                        JSONSerializationUtils
                                            .getJsonObjectSerializer<SessionOrderLines>()
                                            .serialize(it)
                                    )
                                }
                            }
                        )
                        t.billingAddress?.let {
                            put(
                                BILLING_ADDRESS_FIELD,
                                JSONSerializationUtils
                                    .getJsonObjectSerializer<BillingAddressData>()
                                    .serialize(it)
                            )
                        }
                        t.tokenDetails?.let {
                            put(
                                TOKEN_DETAILS_FIELD,
                                JSONSerializationUtils.getJsonObjectSerializer<TokenDetailsData>()
                                    .serialize(
                                        it
                                    )
                            )
                        }
                    }
                }
            }

            @JvmField
            val deserializer = object : JSONDeserializer<SessionData> {

                override fun deserialize(t: JSONObject): SessionData {
                    return SessionData(
                        t.optNullableString(RECURRING_DESCRIPTION_FIELD),
                        t.optNullableString(PURCHASE_COUNTRY_FIELD),
                        t.optNullableString(PURCHASE_CURRENCY_FIELD),
                        t.optNullableString(LOCALE_FIELD),
                        t.optNullableInt(ORDER_AMOUNT_FIELD),
                        t.getJSONArray(ORDER_LINES_FIELD).sequence<JSONObject>()
                            .map {
                                JSONSerializationUtils
                                    .getDeserializer<SessionOrderLines>()
                                    .deserialize(it)
                            }.toList(),
                        t.optJSONObject(BILLING_ADDRESS_FIELD)?.let {
                            JSONSerializationUtils.getDeserializer<BillingAddressData>()
                                .deserialize(it)
                        },
                        t.optJSONObject(TOKEN_DETAILS_FIELD)?.let {
                            JSONSerializationUtils.getDeserializer<TokenDetailsData>()
                                .deserialize(it)
                        }
                    )
                }
            }
        }
    }

    internal data class BillingAddressData(
        val addressLine1: String?,
        val addressLine2: String?,
        val addressLine3: String?,
        val city: String?,
        val countryCode: String?,
        val email: String?,
        val firstName: String?,
        val lastName: String?,
        val phoneNumber: String?,
        val postalCode: String?,
        val state: String?,
        val title: String?
    ) : JSONObjectSerializable, JSONDeserializable {

        companion object {

            private const val ADDRESS_LINE_1_FIELD = "addressLine1"
            private const val ADDRESS_LINE_2_FIELD = "addressLine2"
            private const val ADDRESS_LINE_3_FIELD = "addressLine3"
            private const val CITY_FIELD = "city"
            private const val COUNTRY_CODE_FIELD = "countryCode"
            private const val EMAIL_FIELD = "email"
            private const val FIRST_NAME_FIELD = "firstName"
            private const val LAST_NAME_FIELD = "lastName"
            private const val PHONE_NUMBER_FIELD = "phoneNumber"
            private const val POSTAL_CODE_FIELD = "postalCode"
            private const val STATE_FIELD = "state"
            private const val TITLE_FIELD = "state"

            @JvmField
            val serializer = object : JSONObjectSerializer<BillingAddressData> {

                override fun serialize(t: BillingAddressData): JSONObject {
                    return JSONObject().apply {
                        putOpt(ADDRESS_LINE_1_FIELD, t.addressLine1)
                        putOpt(ADDRESS_LINE_2_FIELD, t.addressLine2)
                        putOpt(ADDRESS_LINE_3_FIELD, t.addressLine3)
                        putOpt(CITY_FIELD, t.city)
                        putOpt(COUNTRY_CODE_FIELD, t.countryCode)
                        putOpt(EMAIL_FIELD, t.email)
                        putOpt(FIRST_NAME_FIELD, t.firstName)
                        putOpt(LAST_NAME_FIELD, t.lastName)
                        putOpt(PHONE_NUMBER_FIELD, t.phoneNumber)
                        putOpt(POSTAL_CODE_FIELD, t.postalCode)
                        putOpt(STATE_FIELD, t.state)
                        putOpt(TITLE_FIELD, t.title)
                    }
                }
            }

            @JvmField
            val deserializer = object : JSONDeserializer<BillingAddressData> {

                override fun deserialize(t: JSONObject): BillingAddressData {
                    return BillingAddressData(
                        t.optNullableString(ADDRESS_LINE_1_FIELD),
                        t.optNullableString(ADDRESS_LINE_2_FIELD),
                        t.optNullableString(ADDRESS_LINE_3_FIELD),
                        t.optNullableString(CITY_FIELD),
                        t.optNullableString(COUNTRY_CODE_FIELD),
                        t.optNullableString(EMAIL_FIELD),
                        t.optNullableString(FIRST_NAME_FIELD),
                        t.optNullableString(LAST_NAME_FIELD),
                        t.optNullableString(PHONE_NUMBER_FIELD),
                        t.optNullableString(POSTAL_CODE_FIELD),
                        t.optNullableString(STATE_FIELD),
                        t.optNullableString(TITLE_FIELD),
                    )
                }
            }
        }
    }

    data class SessionOrderLines(
        val type: String?,
        val name: String?,
        val quantity: Int?,
        val unitPrice: Int?,
        val totalAmount: Int?,
        val totalDiscountAmount: Int?
    ) : JSONObjectSerializable, JSONDeserializable {

        companion object {
            private const val TYPE_FIELD = "type"
            private const val NAME_FIELD = "name"
            private const val QUANTITY_FIELD = "quantity"
            private const val UNIT_PRICE_FIELD = "unit_price"
            private const val TOTAL_AMOUNT_FIELD = "total_amount"
            private const val TOTAL_DISCOUNT_AMOUNT_FIELD = "total_discount_amount"

            @JvmField
            val serializer = object : JSONObjectSerializer<SessionOrderLines> {

                override fun serialize(t: SessionOrderLines): JSONObject {
                    return JSONObject().apply {
                        putOpt(TYPE_FIELD, t.type)
                        putOpt(NAME_FIELD, t.name)
                        putOpt(QUANTITY_FIELD, t.quantity)
                        putOpt(UNIT_PRICE_FIELD, t.unitPrice)
                        putOpt(TOTAL_AMOUNT_FIELD, t.totalAmount)
                        putOpt(TOTAL_DISCOUNT_AMOUNT_FIELD, t.totalDiscountAmount)
                    }
                }
            }

            @JvmField
            val deserializer = object : JSONDeserializer<SessionOrderLines> {

                override fun deserialize(t: JSONObject): SessionOrderLines {
                    return SessionOrderLines(
                        t.optNullableString(TYPE_FIELD),
                        t.optNullableString(NAME_FIELD),
                        t.optNullableInt(QUANTITY_FIELD),
                        t.optNullableInt(UNIT_PRICE_FIELD),
                        t.optNullableInt(TOTAL_AMOUNT_FIELD),
                        t.optNullableInt(TOTAL_DISCOUNT_AMOUNT_FIELD),
                    )
                }
            }
        }
    }

    data class TokenDetailsData(
        val brand: String?,
        val maskedNumber: String?,
        val type: String,
        val expiryDate: String?
    ) : JSONObjectSerializable, JSONDeserializable {

        companion object {
            private const val BRAND_FIELD = "brand"
            private const val MASKED_NUMBER_FIELD = "masked_number"
            private const val TYPE_FIELD = "type"
            private const val EXPIRY_DATE_FIELD = "expiry_date"

            @JvmField
            val serializer = object : JSONObjectSerializer<TokenDetailsData> {

                override fun serialize(t: TokenDetailsData): JSONObject {
                    return JSONObject().apply {
                        putOpt(BRAND_FIELD, t.brand)
                        putOpt(MASKED_NUMBER_FIELD, t.maskedNumber)
                        put(TYPE_FIELD, t.type)
                        putOpt(EXPIRY_DATE_FIELD, t.expiryDate)
                    }
                }
            }

            @JvmField
            val deserializer = object : JSONDeserializer<TokenDetailsData> {

                override fun deserialize(t: JSONObject): TokenDetailsData {
                    return TokenDetailsData(
                        t.optNullableString(BRAND_FIELD),
                        t.optNullableString(MASKED_NUMBER_FIELD),
                        t.getString(TYPE_FIELD),
                        t.optNullableString(EXPIRY_DATE_FIELD),
                    )
                }
            }
        }
    }

    companion object {
        private const val CUSTOMER_TOKEN_ID_FIELD = "customerTokenId"
        private const val SESSION_DATA_FIELD = "sessionData"

        @JvmField
        val deserializer = object : JSONDeserializer<CreateCustomerTokenDataResponse> {

            override fun deserialize(t: JSONObject): CreateCustomerTokenDataResponse {
                return CreateCustomerTokenDataResponse(
                    t.optNullableString(CUSTOMER_TOKEN_ID_FIELD),
                    JSONSerializationUtils.getDeserializer<SessionData>().deserialize(
                        t.getJSONObject(
                            SESSION_DATA_FIELD
                        )
                    )
                )
            }
        }
    }
}
