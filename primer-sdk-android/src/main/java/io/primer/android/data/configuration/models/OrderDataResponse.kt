package io.primer.android.data.configuration.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableInt
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.sequence
import io.primer.android.domain.action.models.PrimerLineItem
import io.primer.android.domain.action.models.PrimerOrder
import org.json.JSONObject

internal data class OrderDataResponse(
    var orderId: String? = null,
    var currencyCode: String? = null,
    var merchantAmount: Int? = null,
    val totalOrderAmount: Int? = null,
    var countryCode: CountryCode? = null,
    var lineItems: List<LineItemDataResponse> = emptyList(),
    val fees: List<FeeDataResponse> = listOf()
) : JSONDeserializable {

    data class LineItemDataResponse(
        val itemId: String? = null,
        val description: String? = null,
        val unitAmount: Int? = null,
        val quantity: Int,
        val discountAmount: Int? = null,
        val taxAmount: Int? = null,
        val taxCode: String? = null
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

            @JvmField
            val deserializer = object : JSONObjectDeserializer<LineItemDataResponse> {

                override fun deserialize(t: JSONObject): LineItemDataResponse {
                    return LineItemDataResponse(
                        t.optNullableString(ITEM_ID_FIELD),
                        t.optNullableString(DESCRIPTION_FIELD),
                        t.optNullableInt(UNIT_AMOUNT_FIELD),
                        t.getInt(QUANTITY_FIELD),
                        t.optNullableInt(DISCOUNT_AMOUNT_FIELD),
                        t.optNullableInt(TAX_AMOUNT_FIELD),
                        t.optNullableString(TAX_CODE_FIELD)
                    )
                }
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

    fun toOrder() = PrimerOrder(countryCode)

    companion object {
        const val ORDER_ID_FIELD = "orderId"
        const val CURRENCY_CODE_FIELD = "currencyCode"
        const val MERCHANT_AMOUNT_FIELD = "merchantAmount"
        const val TOTAL_ORDER_AMOUNT_FIELD = "totalOrderAmount"
        const val COUNTRY_CODE_FIELD = "countryCode"
        const val LINE_ITEMS_FIELD = "lineItems"
        const val FEES_FIELD = "fees"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            OrderDataResponse(
                t.optNullableString(ORDER_ID_FIELD),
                t.optNullableString(CURRENCY_CODE_FIELD),
                t.optNullableInt(MERCHANT_AMOUNT_FIELD),
                t.optNullableInt(TOTAL_ORDER_AMOUNT_FIELD),
                t.optNullableString(COUNTRY_CODE_FIELD)?.let { CountryCode.valueOf(it) },
                t.optJSONArray(LINE_ITEMS_FIELD)?.sequence<JSONObject>()?.map {
                    JSONSerializationUtils.getJsonObjectDeserializer<LineItemDataResponse>()
                        .deserialize(it)
                }?.toList().orEmpty(),
                t.optJSONArray(FEES_FIELD)?.sequence<JSONObject>()?.map {
                    JSONSerializationUtils.getJsonObjectDeserializer<FeeDataResponse>()
                        .deserialize(it)
                }?.toList().orEmpty()
            )
        }
    }
}
