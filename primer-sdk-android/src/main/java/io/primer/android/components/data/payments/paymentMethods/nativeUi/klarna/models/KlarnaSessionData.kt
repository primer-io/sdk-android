package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableInt
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.sequence
import org.json.JSONArray
import org.json.JSONObject

internal data class KlarnaSessionData(
    val recurringDescription: String?,
    val purchaseCountry: String?,
    val purchaseCurrency: String?,
    val locale: String?,
    val orderAmount: Int?,
    val orderLines: List<SessionOrderLines>,
    val billingAddress: AddressData?,
    val shippingAddress: AddressData?,
    val tokenDetails: TokenDetailsData?,
    val orderTaxAmount: Int?
) : JSONObjectSerializable, JSONDeserializable {

    companion object {
        private const val RECURRING_DESCRIPTION_FIELD = "recurringDescription"
        private const val PURCHASE_COUNTRY_FIELD = "purchaseCountry"
        private const val PURCHASE_CURRENCY_FIELD = "purchaseCurrency"
        private const val LOCALE_FIELD = "locale"
        private const val ORDER_AMOUNT_FIELD = "orderAmount"
        const val ORDER_LINES_FIELD = "orderLines"
        private const val BILLING_ADDRESS_FIELD = "billingAddress"
        private const val SHIPPING_ADDRESS_FIELD = "shippingAddress"
        private const val TOKEN_DETAILS_FIELD = "tokenDetails"
        private const val ORDER_TAX_AMOUNT_FIELD = "orderTaxAmount"

        @JvmField
        val serializer =
            JSONObjectSerializer<KlarnaSessionData> { t ->
                JSONObject().apply {
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
                                .getJsonObjectSerializer<AddressData>()
                                .serialize(it)
                        )
                    }
                    t.shippingAddress?.let {
                        put(
                            SHIPPING_ADDRESS_FIELD,
                            JSONSerializationUtils
                                .getJsonObjectSerializer<AddressData>()
                                .serialize(it)
                        )
                    }
                    t.tokenDetails?.let {
                        put(
                            TOKEN_DETAILS_FIELD,
                            JSONSerializationUtils.getJsonObjectSerializer<TokenDetailsData>()
                                .serialize(it)
                        )
                    }
                    putOpt(ORDER_TAX_AMOUNT_FIELD, t.orderTaxAmount)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer<KlarnaSessionData> { t ->
                KlarnaSessionData(
                    recurringDescription = t.optNullableString(RECURRING_DESCRIPTION_FIELD),
                    purchaseCountry = t.optNullableString(PURCHASE_COUNTRY_FIELD),
                    purchaseCurrency = t.optNullableString(PURCHASE_CURRENCY_FIELD),
                    locale = t.optNullableString(LOCALE_FIELD),
                    orderAmount = t.optNullableInt(ORDER_AMOUNT_FIELD),
                    orderLines = t.getJSONArray(ORDER_LINES_FIELD).sequence<JSONObject>()
                        .map {
                            JSONSerializationUtils
                                .getJsonObjectDeserializer<SessionOrderLines>()
                                .deserialize(it)
                        }.toList(),
                    billingAddress = t.optJSONObject(BILLING_ADDRESS_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<AddressData>()
                            .deserialize(it)
                    },
                    shippingAddress = t.optJSONObject(SHIPPING_ADDRESS_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<AddressData>()
                            .deserialize(it)
                    },
                    tokenDetails = t.optJSONObject(TOKEN_DETAILS_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<TokenDetailsData>()
                            .deserialize(it)
                    },
                    orderTaxAmount = t.optNullableInt(ORDER_TAX_AMOUNT_FIELD)
                )
            }
    }
}

internal data class AddressData(
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
        val serializer =
            JSONObjectSerializer<AddressData> { t ->
                JSONObject().apply {
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

        @JvmField
        val deserializer =
            JSONObjectDeserializer<AddressData> { t ->
                AddressData(
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
                    t.optNullableString(TITLE_FIELD)
                )
            }
    }
}

internal data class SessionOrderLines(
    val type: String?,
    val name: String?,
    val quantity: Int?,
    val reference: String?,
    val unitPrice: Int?,
    val totalAmount: Int?,
    val totalDiscountAmount: Int?
) : JSONObjectSerializable, JSONDeserializable {

    companion object {
        const val TYPE_FIELD = "type"
        private const val NAME_FIELD = "name"
        const val QUANTITY_FIELD = "quantity"
        private const val REFERENCE_FIELD = "reference"
        const val UNIT_PRICE_FIELD = "unit_price"
        const val TOTAL_AMOUNT_FIELD = "total_amount"
        const val TOTAL_DISCOUNT_AMOUNT_FIELD = "total_discount_amount"

        @JvmField
        val serializer =
            JSONObjectSerializer<SessionOrderLines> { t ->
                JSONObject().apply {
                    putOpt(TYPE_FIELD, t.type)
                    putOpt(NAME_FIELD, t.name)
                    putOpt(QUANTITY_FIELD, t.quantity)
                    putOpt(REFERENCE_FIELD, t.reference)
                    putOpt(UNIT_PRICE_FIELD, t.unitPrice)
                    putOpt(TOTAL_AMOUNT_FIELD, t.totalAmount)
                    putOpt(TOTAL_DISCOUNT_AMOUNT_FIELD, t.totalDiscountAmount)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer<SessionOrderLines> { t ->
                SessionOrderLines(
                    t.optNullableString(TYPE_FIELD),
                    t.optNullableString(NAME_FIELD),
                    t.optNullableInt(QUANTITY_FIELD),
                    t.optNullableString(REFERENCE_FIELD),
                    t.optNullableInt(UNIT_PRICE_FIELD),
                    t.optNullableInt(TOTAL_AMOUNT_FIELD),
                    t.optNullableInt(TOTAL_DISCOUNT_AMOUNT_FIELD)
                )
            }
    }
}

internal data class TokenDetailsData(
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
        val serializer =
            JSONObjectSerializer<TokenDetailsData> { t ->
                JSONObject().apply {
                    putOpt(BRAND_FIELD, t.brand)
                    putOpt(MASKED_NUMBER_FIELD, t.maskedNumber)
                    put(TYPE_FIELD, t.type)
                    putOpt(EXPIRY_DATE_FIELD, t.expiryDate)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer<TokenDetailsData> { t ->
                TokenDetailsData(
                    t.optNullableString(BRAND_FIELD),
                    t.optNullableString(MASKED_NUMBER_FIELD),
                    t.getString(TYPE_FIELD),
                    t.optNullableString(EXPIRY_DATE_FIELD)
                )
            }
    }
}
