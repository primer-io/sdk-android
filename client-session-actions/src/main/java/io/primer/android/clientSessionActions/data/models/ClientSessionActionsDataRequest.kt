package io.primer.android.clientSessionActions.data.models

import io.primer.android.configuration.data.model.AddressData
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils.serialize
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import org.json.JSONArray
import org.json.JSONObject

internal data class ClientSessionActionsDataRequest(
    val actions: List<Action>,
) : JSONObjectSerializable {
    sealed class Action(
        private val type: String,
        private val params: ActionParams,
    ) : JSONObjectSerializable {
        internal fun toJSONObject(): JSONObject {
            return JSONObject().apply {
                put(TYPE_FIELD, type)
                put(PARAMS_FIELD, params.toJSONObject())
            }
        }

        internal companion object {
            const val TYPE_FIELD = "type"
            const val PARAMS_FIELD = "params"
        }
    }

    private interface ActionParams {
        fun toJSONObject(): JSONObject
    }

    private data class SingleStringParam(private val key: String, private val value: String) : ActionParams {
        override fun toJSONObject() =
            JSONObject().apply {
                put(key, value)
            }
    }

    private object EmptyParams : ActionParams {
        override fun toJSONObject() = JSONObject()
    }

    private class AddressParam(private val key: String, private val address: AddressData) : ActionParams {
        override fun toJSONObject() =
            JSONObject().apply {
                put(key, address.serialize())
            }
    }

    data class SetEmailAddress(val email: String) : Action(
        type = "SET_EMAIL_ADDRESS",
        params = SingleStringParam("emailAddress", email),
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetEmailAddress> { it.toJSONObject() }
        }
    }

    data class SetCustomerFirstName(val firstName: String) : Action(
        type = "SET_CUSTOMER_FIRST_NAME",
        params = SingleStringParam("firstName", firstName),
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetCustomerFirstName> { it.toJSONObject() }
        }
    }

    data class SetCustomerLastName(val lastName: String) : Action(
        type = "SET_CUSTOMER_LAST_NAME",
        params = SingleStringParam("lastName", lastName),
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetCustomerLastName> { it.toJSONObject() }
        }
    }

    data class SetMobileNumber(val mobileNumber: String) : Action(
        type = "SET_MOBILE_NUMBER",
        params = SingleStringParam("mobileNumber", mobileNumber),
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetMobileNumber> { it.toJSONObject() }
        }
    }

    data class SetPaymentMethod(
        val paymentMethodType: String,
        val binData: BinData? = null,
    ) : Action(
        type = "SELECT_PAYMENT_METHOD",
        params =
        object : ActionParams {
            private val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
            private val BIN_DATA_FIELD = "binData"

            override fun toJSONObject() =
                JSONObject().apply {
                    put(PAYMENT_METHOD_TYPE_FIELD, paymentMethodType)
                    putOpt(
                        BIN_DATA_FIELD,
                        binData?.serialize(),
                    )
                }
        },
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetPaymentMethod> { it.toJSONObject() }
        }
    }

    data object UnsetPaymentMethod : Action(
        type = "UNSELECT_PAYMENT_METHOD",
        params = EmptyParams,
    ) {
        @JvmField
        val serializer = JSONObjectSerializer<UnsetPaymentMethod> { it.toJSONObject() }
    }

    data class SetBillingAddress(val address: AddressData) : Action(
        type = "SET_BILLING_ADDRESS",
        params = AddressParam("billingAddress", address),
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetBillingAddress> { it.toJSONObject() }
        }
    }

    data class SetShippingAddress(val address: AddressData) : Action(
        type = "SET_SHIPPING_ADDRESS",
        params = AddressParam("shippingAddress", address),
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetShippingAddress> { it.toJSONObject() }
        }
    }

    data class SetShippingMethodId(val shippingMethodId: String) : Action(
        type = "SELECT_SHIPPING_METHOD",
        params = SingleStringParam("shipping_method_id", shippingMethodId),
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetShippingMethodId> { it.toJSONObject() }
        }
    }

    companion object {
        private const val ACTIONS_FIELD = "actions"

        @JvmField
        val serializer =
            JSONObjectSerializer<ClientSessionActionsDataRequest> { request ->
                JSONObject().apply {
                    put(
                        ACTIONS_FIELD,
                        JSONArray().apply {
                            request.actions.forEach { put(it.toJSONObject()) }
                        },
                    )
                }
            }
    }
}

internal data class BinData(
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
            JSONObjectDeserializer<BinData> { t ->
                BinData(
                    t.optNullableString(NETWORK_FIELD),
                )
            }
    }
}
