package io.primer.android.klarna.implementation.session.data.models

import io.primer.android.configuration.data.model.AddressData
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableInt
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.core.data.serialization.json.extensions.sequence
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
        val serializer = JSONObjectSerializer<KlarnaSessionData> { t ->
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
        val deserializer = JSONObjectDeserializer { t ->
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
            JSONObjectDeserializer { t ->
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
            JSONObjectDeserializer { t ->
                TokenDetailsData(
                    t.optNullableString(BRAND_FIELD),
                    t.optNullableString(MASKED_NUMBER_FIELD),
                    t.getString(TYPE_FIELD),
                    t.optNullableString(EXPIRY_DATE_FIELD)
                )
            }
    }
}
