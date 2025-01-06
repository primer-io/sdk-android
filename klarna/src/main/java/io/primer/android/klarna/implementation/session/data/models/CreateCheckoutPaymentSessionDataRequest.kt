package io.primer.android.klarna.implementation.session.data.models

import io.primer.android.configuration.data.model.AddressData
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import org.json.JSONArray
import org.json.JSONObject

internal data class CreateCheckoutPaymentSessionDataRequest(
    val paymentMethodConfigId: String,
    val sessionType: KlarnaSessionType,
    val totalAmount: Int,
    val localeData: LocaleDataRequest,
    val orderItems: List<OrderItem>,
    val billingAddress: AddressData?,
    val shippingAddress: AddressData?,
) : JSONObjectSerializable {
    internal data class OrderItem(
        val name: String,
        val unitAmount: Int,
        val reference: String?,
        val quantity: Int,
        val discountAmount: Int?,
        val productType: String?,
        val taxAmount: Int?,
    ) : JSONObjectSerializable {
        companion object {
            private const val NAME_FIELD = "name"
            const val UNIT_AMOUNT_FIELD = "unitAmount"
            private const val REFERENCE_FIELD = "reference"
            const val QUANTITY_FIELD = "quantity"
            const val DISCOUNT_AMOUNT_FIELD = "discountAmount"
            private const val PRODUCT_TYPE_FIELD = "productType"
            const val TAX_AMOUNT_FIELD = "taxAmount"

            @JvmField
            val serializer =
                JSONObjectSerializer<OrderItem> { t ->
                    JSONObject().apply {
                        put(NAME_FIELD, t.name)
                        put(UNIT_AMOUNT_FIELD, t.unitAmount)
                        put(REFERENCE_FIELD, t.reference)
                        put(QUANTITY_FIELD, t.quantity)
                        put(DISCOUNT_AMOUNT_FIELD, t.discountAmount)
                        put(PRODUCT_TYPE_FIELD, t.productType)
                        put(TAX_AMOUNT_FIELD, t.taxAmount)
                    }
                }
        }
    }

    companion object {
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val SESSION_TYPE_FIELD = "sessionType"
        private const val TOTAL_AMOUNT_FIELD = "totalAmount"
        private const val LOCALE_DATA_FIELD = "localeData"
        private const val ORDER_ITEMS_FIELD = "orderItems"
        private const val BILLING_ADDRESS_FIELD = "billingAddress"
        private const val SHIPPING_ADDRESS_FIELD = "shippingAddress"

        val provider =
            object : WhitelistedHttpBodyKeysProvider {
                override val values: List<WhitelistedKey> =
                    whitelistedKeys {
                        primitiveKey(PAYMENT_METHOD_CONFIG_ID_FIELD)
                        primitiveKey(SESSION_TYPE_FIELD)
                        primitiveKey(TOTAL_AMOUNT_FIELD)
                        nonPrimitiveKey(LOCALE_DATA_FIELD) {
                            primitiveKey(LocaleDataRequest.COUNTRY_CODE_FIELD)
                            primitiveKey(LocaleDataRequest.CURRENCY_CODE_FIELD)
                            primitiveKey(LocaleDataRequest.LOCALE_CODE_FIELD)
                        }
                        nonPrimitiveKey(ORDER_ITEMS_FIELD) {
                            primitiveKey(OrderItem.UNIT_AMOUNT_FIELD)
                            primitiveKey(OrderItem.QUANTITY_FIELD)
                            primitiveKey(OrderItem.DISCOUNT_AMOUNT_FIELD)
                            primitiveKey(OrderItem.TAX_AMOUNT_FIELD)
                        }
                    }
            }

        @JvmField
        val serializer =
            JSONObjectSerializer<CreateCheckoutPaymentSessionDataRequest> { t ->
                JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(SESSION_TYPE_FIELD, t.sessionType.name)
                    put(TOTAL_AMOUNT_FIELD, t.totalAmount)
                    put(
                        LOCALE_DATA_FIELD,
                        JSONSerializationUtils.getJsonObjectSerializer<LocaleDataRequest>()
                            .serialize(t.localeData),
                    )
                    val serializer = JSONSerializationUtils.getJsonObjectSerializer<OrderItem>()
                    put(
                        ORDER_ITEMS_FIELD,
                        JSONArray(t.orderItems.map(serializer::serialize)),
                    )
                    t.billingAddress?.let {
                        put(
                            BILLING_ADDRESS_FIELD,
                            JSONSerializationUtils
                                .getJsonObjectSerializer<AddressData>()
                                .serialize(it),
                        )
                    }
                    t.shippingAddress?.let {
                        put(
                            SHIPPING_ADDRESS_FIELD,
                            JSONSerializationUtils
                                .getJsonObjectSerializer<AddressData>()
                                .serialize(it),
                        )
                    }
                }
            }
    }
}
