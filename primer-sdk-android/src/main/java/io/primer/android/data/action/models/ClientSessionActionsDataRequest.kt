package io.primer.android.data.action.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.data.models.EmptyDataRequest
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.data.tokenization.models.BinData
import io.primer.android.domain.action.models.ActionUpdateBillingAddressParams
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.threeds.data.models.Address
import org.json.JSONArray
import org.json.JSONObject

internal data class ClientSessionActionsDataRequest(
    val actions: List<Action>
) : JSONSerializable {

    sealed class Action : JSONSerializable

    data class SetPaymentMethod(val params: SetPaymentMethodRequestDataParams) : Action() {
        companion object {

            @JvmField
            val serializer = object : JSONSerializer<SetPaymentMethod> {
                override fun serialize(t: SetPaymentMethod): JSONObject {
                    return JSONObject().apply {
                        put(TYPE_FIELD, "SELECT_PAYMENT_METHOD")
                        put(
                            PARAMS_FIELD,
                            JSONSerializationUtils
                                .getSerializer<SetPaymentMethodRequestDataParams>()
                                .serialize(t.params)
                        )
                    }
                }
            }
        }
    }

    data class SetPaymentMethodRequestDataParams(
        val paymentMethodType: String,
        val binData: BinData? = null
    ) : JSONSerializable {
        companion object {

            private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
            private const val BIN_DATA_FIELD = "binData"

            @JvmField
            val serializer = object : JSONSerializer<SetPaymentMethodRequestDataParams> {
                override fun serialize(t: SetPaymentMethodRequestDataParams): JSONObject {
                    return JSONObject().apply {
                        put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                        putOpt(
                            BIN_DATA_FIELD,
                            t.binData?.let {
                                JSONSerializationUtils.getSerializer<BinData>()
                                    .serialize(t.binData)
                            }
                        )
                    }
                }
            }
        }
    }

    data class UnsetPaymentMethod(val params: Unit = Unit) : Action() {
        companion object {

            @JvmField
            val serializer = object : JSONSerializer<UnsetPaymentMethod> {
                override fun serialize(t: UnsetPaymentMethod): JSONObject {
                    return JSONObject().apply {
                        put(TYPE_FIELD, "UNSELECT_PAYMENT_METHOD")
                        put(
                            PARAMS_FIELD,
                            JSONSerializationUtils.getSerializer<EmptyDataRequest>()
                                .serialize(EmptyDataRequest())
                        )
                    }
                }
            }
        }
    }

    class SetBillingAddress(
        val params: SetBillingAddressRequestDataParams
    ) : Action() {
        companion object {

            @JvmField
            val serializer = object : JSONSerializer<SetBillingAddress> {
                override fun serialize(t: SetBillingAddress): JSONObject {
                    return JSONObject().apply {
                        put(TYPE_FIELD, "SET_BILLING_ADDRESS")
                        put(
                            PARAMS_FIELD,
                            JSONSerializationUtils
                                .getSerializer<SetBillingAddressRequestDataParams>()
                                .serialize(t.params)
                        )
                    }
                }
            }
        }
    }

    data class SetBillingAddressRequestDataParams(
        val billingAddress: Address
    ) : JSONSerializable {

        companion object {

            private const val BILLING_ADDRESS_FIELD = "billingAddress"

            @JvmField
            val serializer = object : JSONSerializer<SetBillingAddressRequestDataParams> {
                override fun serialize(t: SetBillingAddressRequestDataParams): JSONObject {
                    return JSONObject().apply {
                        put(
                            BILLING_ADDRESS_FIELD,
                            JSONSerializationUtils
                                .getSerializer<Address>()
                                .serialize(t.billingAddress)
                        )
                    }
                }
            }
        }
    }

    companion object {
        // common
        private const val TYPE_FIELD = "type"
        private const val PARAMS_FIELD = "params"
        private const val ACTIONS_FIELD = "actions"

        @JvmField
        val serializer = object : JSONSerializer<ClientSessionActionsDataRequest> {
            override fun serialize(t: ClientSessionActionsDataRequest): JSONObject {
                return JSONObject().apply {
                    put(
                        ACTIONS_FIELD,
                        JSONArray().apply {
                            t.actions.map { action ->
                                put(
                                    when (action) {
                                        is SetPaymentMethod ->
                                            JSONSerializationUtils.getSerializer<SetPaymentMethod>()
                                                .serialize(action)
                                        is UnsetPaymentMethod ->
                                            JSONSerializationUtils
                                                .getSerializer<UnsetPaymentMethod>()
                                                .serialize(action)
                                        is SetBillingAddress ->
                                            JSONSerializationUtils
                                                .getSerializer<SetBillingAddress>()
                                                .serialize(action)
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

internal fun BaseActionUpdateParams.toActionData() = when (this) {
    is ActionUpdateSelectPaymentMethodParams -> ClientSessionActionsDataRequest.SetPaymentMethod(
        ClientSessionActionsDataRequest.SetPaymentMethodRequestDataParams(
            paymentMethodType,
            cardNetwork?.let { BinData(it) }
        )
    )
    is ActionUpdateUnselectPaymentMethodParams ->
        ClientSessionActionsDataRequest.UnsetPaymentMethod()
    is ActionUpdateBillingAddressParams -> ClientSessionActionsDataRequest.SetBillingAddress(
        ClientSessionActionsDataRequest.SetBillingAddressRequestDataParams(
            Address(
                firstName = firstName,
                lastName = lastName,
                addressLine1 = addressLine1.orEmpty(),
                addressLine2 = addressLine2,
                postalCode = postalCode.orEmpty(),
                city = city.orEmpty(),
                countryCode = countryCode.orEmpty()
            )
        )
    )
}
