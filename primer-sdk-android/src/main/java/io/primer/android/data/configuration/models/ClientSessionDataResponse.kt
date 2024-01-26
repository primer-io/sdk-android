package io.primer.android.data.configuration.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableBoolean
import io.primer.android.core.serialization.json.extensions.optNullableInt
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.sequence
import io.primer.android.domain.ClientSessionData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.session.models.ClientSession
import org.json.JSONObject

internal data class ClientSessionDataResponse(
    val clientSessionId: String?,
    val customerId: String?,
    val orderId: String?,
    val testId: String?,
    val amount: Int?,
    val currencyCode: String?,
    val customer: CustomerDataResponse?,
    val order: OrderDataResponse?,
    val paymentMethod: PaymentMethodDataResponse?
) : JSONDeserializable {

    data class PaymentMethodDataResponse(
        val vaultOnSuccess: Boolean?,
        val options: List<PaymentMethodOptionDataResponse>
    ) : JSONDeserializable {

        val surcharges: Map<String, Int>
            get() {
                val map = mutableMapOf<String, Int>()
                options.forEach { option ->
                    if (option.type == PaymentMethodType.PAYMENT_CARD.name) {
                        option.networks?.forEach { network ->
                            map[network.type] = network.surcharge
                        }
                    } else {
                        map[option.type] = option.surcharge ?: 0
                    }
                }
                return map
            }

        companion object {
            const val VAULT_ON_SUCCESS_FIELD = "vaultOnSuccess"
            const val OPTIONS_FIELD = "options"

            @JvmField
            val deserializer = object : JSONDeserializer<PaymentMethodDataResponse> {

                override fun deserialize(t: JSONObject): PaymentMethodDataResponse {
                    return PaymentMethodDataResponse(
                        t.optNullableBoolean(VAULT_ON_SUCCESS_FIELD),
                        t.optJSONArray(OPTIONS_FIELD)?.sequence<JSONObject>()?.map {
                            JSONSerializationUtils
                                .getDeserializer<PaymentMethodOptionDataResponse>()
                                .deserialize(it)
                        }?.toList().orEmpty()
                    )
                }
            }
        }
    }

    // todo: may be better to use sealed class/polymorphism
    data class PaymentMethodOptionDataResponse(
        val type: String,
        val surcharge: Int?,
        val networks: List<NetworkOptionDataResponse>?
    ) : JSONDeserializable {
        companion object {
            const val TYPE_FIELD = "type"
            const val SURCHARGE_FIELD = "surcharge"
            const val NETWORKS_FIELD = "networks"

            @JvmField
            val deserializer = object : JSONDeserializer<PaymentMethodOptionDataResponse> {

                override fun deserialize(t: JSONObject): PaymentMethodOptionDataResponse {
                    return PaymentMethodOptionDataResponse(
                        t.getString(TYPE_FIELD),
                        t.optNullableInt(SURCHARGE_FIELD),
                        t.optJSONArray(NETWORKS_FIELD)?.sequence<JSONObject>()?.map {
                            JSONSerializationUtils.getDeserializer<NetworkOptionDataResponse>()
                                .deserialize(it)
                        }?.toList()
                    )
                }
            }
        }
    }

    data class NetworkOptionDataResponse(
        val type: String,
        val surcharge: Int
    ) : JSONDeserializable {
        companion object {
            const val TYPE_FIELD = "type"
            const val SURCHARGE_FIELD = "surcharge"

            @JvmField
            val deserializer = object : JSONDeserializer<NetworkOptionDataResponse> {

                override fun deserialize(t: JSONObject): NetworkOptionDataResponse {
                    return NetworkOptionDataResponse(
                        t.getString(TYPE_FIELD),
                        t.getInt(SURCHARGE_FIELD)
                    )
                }
            }
        }
    }

    fun toClientSessionData() = ClientSessionData(
        PrimerClientSession(
            customer?.customerId ?: customerId,
            order?.orderId ?: orderId,
            order?.currencyCode ?: currencyCode,
            order?.totalOrderAmount ?: amount,
            order?.lineItems?.map { it.toLineItem() },
            order?.toOrder(),
            customer?.toCustomer()
        )
    )

    fun toClientSession() = ClientSession(this)

    companion object {
        const val CLIENT_SESSION_ID_FIELD = "clientSessionId"
        private const val CUSTOMER_ID_FIELD = "customerId"
        private const val ORDER_ID_FIELD = "orderId"
        private const val TEST_ID_FIELD = "testId"
        private const val AMOUNT_FIELD = "amount"
        private const val CURRENCY_CODE_FIELD = "currencyCode"
        private const val CUSTOMER_DATA_FIELD = "customer"
        const val ORDER_DATA_FIELD = "order"
        const val PAYMENT_METHOD_DATA_FIELD = "paymentMethod"

        @JvmField
        val deserializer = object : JSONDeserializer<ClientSessionDataResponse> {

            override fun deserialize(t: JSONObject): ClientSessionDataResponse {
                return ClientSessionDataResponse(
                    t.optNullableString(CLIENT_SESSION_ID_FIELD),
                    t.optNullableString(CUSTOMER_ID_FIELD),
                    t.optNullableString(ORDER_ID_FIELD),
                    t.optNullableString(TEST_ID_FIELD),
                    t.optNullableInt(AMOUNT_FIELD),
                    t.optNullableString(CURRENCY_CODE_FIELD),
                    t.optJSONObject(CUSTOMER_DATA_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<CustomerDataResponse>()
                            .deserialize(it)
                    },
                    t.optJSONObject(ORDER_DATA_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<OrderDataResponse>()
                            .deserialize(it)
                    },
                    t.optJSONObject(PAYMENT_METHOD_DATA_FIELD)?.let {
                        JSONSerializationUtils
                            .getDeserializer<PaymentMethodDataResponse>()
                            .deserialize(it)
                    }
                )
            }
        }
    }
}
