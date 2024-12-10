package io.primer.android.configuration.data.model

import io.primer.android.domain.action.models.PrimerFee
import io.primer.android.domain.action.models.PrimerLineItem
import io.primer.android.domain.action.models.PrimerOrder
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils.deserialize
import io.primer.android.core.data.serialization.json.extensions.optNullableInt
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.core.data.serialization.json.extensions.sequence
import io.primer.android.domain.action.models.PrimerShipping
import org.json.JSONObject

data class OrderDataResponse(
    var orderId: String? = null,
    var currencyCode: String? = null,
    var merchantAmount: Int? = null,
    val totalOrderAmount: Int? = null,
    var countryCode: CountryCode? = null,
    var lineItems: List<LineItemDataResponse> = emptyList(),
    val fees: List<FeeDataResponse> = listOf(),
    val shipping: ShippingDataResponse? = null
) : JSONDeserializable {

    val currentAmount: Int
        @Throws
        get() = merchantAmount ?: totalOrderAmount ?: throw IllegalArgumentException(AMOUNT_EXCEPTION)

    data class LineItemDataResponse(
        val itemId: String? = null,
        val description: String? = null,
        val unitAmount: Int? = null,
        val quantity: Int,
        val discountAmount: Int? = null,
        val taxAmount: Int? = null,
        val taxCode: String? = null,
        val productType: String? = null
    ) : JSONDeserializable {
        fun toLineItem() = PrimerLineItem(
            itemId,
            description,
            unitAmount,
            discountAmount,
            quantity,
            taxCode,
            taxAmount
        )

        companion object {
            const val ITEM_ID_FIELD = "itemId"
            private const val DESCRIPTION_FIELD = "description"
            const val UNIT_AMOUNT_FIELD = "amount"
            const val QUANTITY_FIELD = "quantity"
            const val DISCOUNT_AMOUNT_FIELD = "discountAmount"
            const val TAX_AMOUNT_FIELD = "taxAmount"
            const val TAX_CODE_FIELD = "taxCode"
            private const val PRODUCT_TYPE_FIELD = "productType"

            @JvmField
            val deserializer = JSONObjectDeserializer { t ->
                LineItemDataResponse(
                    t.optNullableString(ITEM_ID_FIELD),
                    t.optNullableString(DESCRIPTION_FIELD),
                    t.optNullableInt(UNIT_AMOUNT_FIELD),
                    t.getInt(QUANTITY_FIELD),
                    t.optNullableInt(DISCOUNT_AMOUNT_FIELD),
                    t.optNullableInt(TAX_AMOUNT_FIELD),
                    t.optNullableString(TAX_CODE_FIELD),
                    t.optNullableString(PRODUCT_TYPE_FIELD)
                )
            }
        }
    }

    data class FeeDataResponse(
        val type: String?,
        val amount: Int
    ) : JSONDeserializable {

        companion object {
            private const val TYPE_FIELD = "type"
            private const val AMOUNT_FIELD = "amount"

            @JvmField
            val deserializer = JSONObjectDeserializer { t ->
                FeeDataResponse(
                    t.optNullableString(TYPE_FIELD),
                    t.getInt(AMOUNT_FIELD)
                )
            }
        }
    }

    data class ShippingDataResponse(
        val amount: Int? = null,
        val methodId: String? = null,
        val methodName: String? = null,
        val methodDescription: String? = null
    ) : JSONDeserializable {

        fun toShippingData() = PrimerShipping(
            amount = this.amount,
            methodId = this.methodId,
            methodName = this.methodName,
            methodDescription = this.methodDescription
        )

        companion object {
            private const val AMOUNT_FIELD = "amount"
            private const val METHOD_ID_FIELD = "methodId"
            private const val METHOD_NAME_FIELD = "methodName"
            private const val METHOD_DESCRIPTION_FIELD = "methodDescription"

            @JvmField
            val deserializer = JSONObjectDeserializer { t ->
                ShippingDataResponse(
                    t.optNullableInt(AMOUNT_FIELD),
                    t.optNullableString(METHOD_ID_FIELD),
                    t.optNullableString(METHOD_NAME_FIELD),
                    t.optNullableString(METHOD_DESCRIPTION_FIELD)
                )
            }
        }
    }

    fun toOrder() = PrimerOrder(countryCode)

    fun toFees() = fees.map { PrimerFee(type = it.type, amount = it.amount) }

    companion object {
        const val ORDER_ID_FIELD = "orderId"
        const val CURRENCY_CODE_FIELD = "currencyCode"
        const val MERCHANT_AMOUNT_FIELD = "merchantAmount"
        const val TOTAL_ORDER_AMOUNT_FIELD = "totalOrderAmount"
        const val COUNTRY_CODE_FIELD = "countryCode"
        const val LINE_ITEMS_FIELD = "lineItems"
        const val FEES_FIELD = "fees"
        const val SHIPPING_FIELD = "shipping"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            OrderDataResponse(
                t.optNullableString(ORDER_ID_FIELD),
                t.optNullableString(CURRENCY_CODE_FIELD),
                t.optNullableInt(MERCHANT_AMOUNT_FIELD),
                t.optNullableInt(TOTAL_ORDER_AMOUNT_FIELD),
                t.optNullableString(COUNTRY_CODE_FIELD)?.let { CountryCode.valueOf(it) },
                t.optJSONArray(LINE_ITEMS_FIELD)?.sequence<JSONObject>()?.map {
                    it.deserialize<LineItemDataResponse>()
                }?.toList().orEmpty(),
                t.optJSONArray(FEES_FIELD)?.sequence<JSONObject>()?.map {
                    it.deserialize<FeeDataResponse>()
                }?.toList().orEmpty(),
                t.optJSONObject(SHIPPING_FIELD)?.deserialize()
            )
        }

        private const val EXCEPTION_MESSAGE = "required but not found. Please set this value"
        private const val DOCS_REFERENCE = """when generating the client session with 
POST /client-session. See documentation here: https://primer.io/docs/api#tag/Client-Session"""
        const val AMOUNT_EXCEPTION = "Amount $EXCEPTION_MESSAGE $DOCS_REFERENCE"
    }
}
