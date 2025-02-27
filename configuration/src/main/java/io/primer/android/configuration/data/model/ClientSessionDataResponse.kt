package io.primer.android.configuration.data.model

import io.primer.android.configuration.domain.model.ClientSession
import io.primer.android.configuration.domain.model.ClientSessionData
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableBoolean
import io.primer.android.core.data.serialization.json.extensions.optNullableInt
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.core.data.serialization.json.extensions.sequence
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.action.models.PrimerPaymentMethod
import org.json.JSONObject

data class ClientSessionDataResponse(
    val clientSessionId: String?,
    val customerId: String?,
    val orderId: String?,
    val testId: String?,
    val amount: Int?,
    val currencyCode: String?,
    val customer: CustomerDataResponse?,
    val order: OrderDataResponse?,
    val paymentMethod: PaymentMethodDataResponse?,
) : JSONDeserializable {
    data class PaymentMethodDataResponse(
        val vaultOnSuccess: Boolean?,
        val vaultOnAgreement: Boolean?,
        val options: List<PaymentMethodOptionDataResponse>,
        val orderedAllowedCardNetworks: List<CardNetwork.Type>,
    ) : JSONDeserializable {
        val surcharges: Map<String, Int>
            get() {
                val map = mutableMapOf<String, Int>()
                options.forEach { option ->
                    if (option.type == PAYMENT_CARD_TYPE) {
                        option.networks?.forEach { network ->
                            map[network.type] = network.surcharge
                        }
                    } else {
                        map[option.type] = option.surcharge ?: 0
                    }
                }
                return map
            }

        fun toPrimerPaymentMethod() = PrimerPaymentMethod(orderedAllowedCardNetworks)

        companion object {
            const val PAYMENT_CARD_TYPE = "PAYMENT_CARD"

            const val VAULT_ON_SUCCESS_FIELD = "vaultOnSuccess"
            const val VAULT_ON_AGREEMENT_FIELD = "vaultOnAgreement"
            const val OPTIONS_FIELD = "options"
            const val ALLOWED_CARD_NETWORKS_FIELD = "orderedAllowedCardNetworks"

            @JvmField
            val deserializer =
                JSONObjectDeserializer { t ->
                    PaymentMethodDataResponse(
                        t.optNullableBoolean(VAULT_ON_SUCCESS_FIELD),
                        t.optNullableBoolean(VAULT_ON_AGREEMENT_FIELD),
                        t.optJSONArray(OPTIONS_FIELD)?.sequence<JSONObject>()?.map {
                            JSONSerializationUtils
                                .getJsonObjectDeserializer<PaymentMethodOptionDataResponse>()
                                .deserialize(it)
                        }?.toList().orEmpty(),
                        t.optJSONArray(ALLOWED_CARD_NETWORKS_FIELD)?.sequence<String>()
                            ?.map { network ->
                                CardNetwork.Type.valueOrNull(network)
                            }?.toList().orEmpty().filterNotNull(),
                    )
                }
        }
    }

    // todo: may be better to use sealed class/polymorphism
    data class PaymentMethodOptionDataResponse(
        val type: String,
        val surcharge: Int?,
        val networks: List<NetworkOptionDataResponse>?,
    ) : JSONDeserializable {
        companion object {
            const val TYPE_FIELD = "type"
            const val SURCHARGE_FIELD = "surcharge"
            const val NETWORKS_FIELD = "networks"

            @JvmField
            val deserializer =
                JSONObjectDeserializer { t ->
                    PaymentMethodOptionDataResponse(
                        t.getString(TYPE_FIELD),
                        t.optNullableInt(SURCHARGE_FIELD),
                        t.optJSONArray(NETWORKS_FIELD)?.sequence<JSONObject>()?.map {
                            JSONSerializationUtils
                                .getJsonObjectDeserializer<NetworkOptionDataResponse>()
                                .deserialize(it)
                        }?.toList(),
                    )
                }
        }
    }

    data class NetworkOptionDataResponse(
        val type: String,
        val surcharge: Int,
    ) : JSONDeserializable {
        companion object {
            const val TYPE_FIELD = "type"
            const val SURCHARGE_FIELD = "surcharge"

            @JvmField
            val deserializer =
                JSONObjectDeserializer { t ->
                    NetworkOptionDataResponse(
                        t.getString(TYPE_FIELD),
                        t.getInt(SURCHARGE_FIELD),
                    )
                }
        }
    }

    fun toClientSessionData() =
        ClientSessionData(
            PrimerClientSession(
                customerId = customer?.customerId ?: customerId,
                orderId = order?.orderId ?: orderId,
                currencyCode = order?.currencyCode ?: currencyCode,
                totalAmount = order?.totalOrderAmount ?: amount,
                lineItems = order?.lineItems?.map { it.toLineItem() },
                orderDetails = order?.toOrder(),
                customer = customer?.toCustomer(),
                paymentMethod = paymentMethod?.toPrimerPaymentMethod(),
                fees = order?.toFees(),
            ),
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
        val deserializer =
            JSONObjectDeserializer { t ->
                ClientSessionDataResponse(
                    t.optNullableString(CLIENT_SESSION_ID_FIELD),
                    t.optNullableString(CUSTOMER_ID_FIELD),
                    t.optNullableString(ORDER_ID_FIELD),
                    t.optNullableString(TEST_ID_FIELD),
                    t.optNullableInt(AMOUNT_FIELD),
                    t.optNullableString(CURRENCY_CODE_FIELD),
                    t.optJSONObject(CUSTOMER_DATA_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<CustomerDataResponse>()
                            .deserialize(it)
                    },
                    t.optJSONObject(ORDER_DATA_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<OrderDataResponse>()
                            .deserialize(it)
                    },
                    t.optJSONObject(PAYMENT_METHOD_DATA_FIELD)?.let {
                        JSONSerializationUtils
                            .getJsonObjectDeserializer<PaymentMethodDataResponse>()
                            .deserialize(it)
                    },
                )
            }
    }
}
