package io.primer.android.data.action.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils.serialize
import io.primer.android.data.tokenization.models.BinData
import io.primer.android.threeds.data.models.auth.Address
import org.json.JSONArray
import org.json.JSONObject

internal data class ClientSessionActionsDataRequest(
    val actions: List<Action>
) : JSONObjectSerializable {

    companion object {
        private const val ACTIONS_FIELD = "actions"

        @JvmField
        val serializer = JSONObjectSerializer<ClientSessionActionsDataRequest> { request ->
            JSONObject().apply {
                put(
                    ACTIONS_FIELD,
                    JSONArray().apply {
                        request.actions.forEach { put(it.toJSONObject()) }
                    }
                )
            }
        }
    }

    internal sealed class Action(
        private val type: String,
        private val params: ActionParams
    ) : JSONObjectSerializable {

        internal fun toJSONObject(): JSONObject {
            return JSONObject().apply {
                put(TYPE_FIELD, type)
                put(PARAMS_FIELD, params.toJSONObject())
            }
        }

        companion object {
            private const val TYPE_FIELD = "type"
            private const val PARAMS_FIELD = "params"
        }
    }

    private interface ActionParams {
        fun toJSONObject(): JSONObject
    }

    private data class SingleStringParam(private val key: String, private val value: String) : ActionParams {
        override fun toJSONObject() = JSONObject().apply {
            put(key, value)
        }
    }

    private object EmptyParams : ActionParams {
        override fun toJSONObject() = JSONObject()
    }

    private class AddressParam(private val key: String, private val address: Address) : ActionParams {
        override fun toJSONObject() = JSONObject().apply {
            put(key, address.serialize())
        }
    }

    data class SetEmailAddress(val email: String) : Action(
        type = "SET_EMAIL_ADDRESS",
        params = SingleStringParam("emailAddress", email)
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetEmailAddress> { it.toJSONObject() }
        }
    }

    data class SetCustomerFirstName(val firstName: String) : Action(
        type = "SET_CUSTOMER_FIRST_NAME",
        params = SingleStringParam("firstName", firstName)
    ) {
        companion object {

            @JvmField
            val serializer = JSONObjectSerializer<SetCustomerFirstName> { it.toJSONObject() }
        }
    }

    data class SetCustomerLastName(val lastName: String) : Action(
        type = "SET_CUSTOMER_LAST_NAME",
        params = SingleStringParam("lastName", lastName)
    ) {
        companion object {

            @JvmField
            val serializer = JSONObjectSerializer<SetCustomerLastName> { it.toJSONObject() }
        }
    }

    data class SetMobileNumber(val mobileNumber: String) : Action(
        type = "SET_MOBILE_NUMBER",
        params = SingleStringParam("mobileNumber", mobileNumber)
    ) {
        companion object {

            @JvmField
            val serializer = JSONObjectSerializer<SetMobileNumber> { it.toJSONObject() }
        }
    }

    data class SetPaymentMethod(
        val paymentMethodType: String,
        val binData: BinData? = null
    ) : Action(
        type = "SELECT_PAYMENT_METHOD",
        params = object : ActionParams {
            private val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
            private val BIN_DATA_FIELD = "binData"

            override fun toJSONObject() = JSONObject().apply {
                put(PAYMENT_METHOD_TYPE_FIELD, paymentMethodType)
                putOpt(
                    BIN_DATA_FIELD,
                    binData?.serialize()
                )
            }
        }
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetPaymentMethod> { it.toJSONObject() }
        }
    }

    data object UnsetPaymentMethod : Action(
        type = "UNSELECT_PAYMENT_METHOD",
        params = EmptyParams
    ) {
        @JvmField
        val serializer = JSONObjectSerializer<UnsetPaymentMethod> { it.toJSONObject() }
    }

    data class SetBillingAddress(val address: Address) : Action(
        type = "SET_BILLING_ADDRESS",
        params = AddressParam("billingAddress", address)
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetBillingAddress> { it.toJSONObject() }
        }
    }

    data class SetShippingAddress(val address: Address) : Action(
        type = "SET_SHIPPING_ADDRESS",
        params = AddressParam("shippingAddress", address)
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetShippingAddress> { it.toJSONObject() }
        }
    }

    data class SetShippingMethodId(val shippingMethodId: String) : Action(
        type = "SELECT_SHIPPING_METHOD",
        params = SingleStringParam("shipping_method_id", shippingMethodId)
    ) {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetShippingMethodId> { it.toJSONObject() }
        }
    }
}
