package io.primer.android.data.action.models

import io.primer.android.core.data.models.EmptyDataRequest
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.data.tokenization.models.BinData
import io.primer.android.domain.action.models.ActionUpdateBillingAddressParams
import io.primer.android.domain.action.models.ActionUpdateCustomerDetailsParams
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.threeds.data.models.auth.Address
import org.json.JSONArray
import org.json.JSONObject

internal data class ClientSessionActionsDataRequest(
    val actions: List<Action>
) : JSONObjectSerializable {

    sealed class Action : JSONObjectSerializable

    data class SetEmailAddress(
        val params: SetEmailAddressRequestDataParams
    ) : Action() {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetEmailAddress> { t ->
                JSONObject().apply {
                    put(TYPE_FIELD, "SET_EMAIL_ADDRESS")
                    put(
                        PARAMS_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<SetEmailAddressRequestDataParams>()
                            .serialize(t.params)
                    )
                }
            }
        }
    }

    data class SetEmailAddressRequestDataParams(
        val emailAddress: String
    ) : JSONObjectSerializable {
        companion object {
            private const val EMAIL_ADDRESS_FIELD = "emailAddress"

            @JvmField
            val serializer = JSONObjectSerializer<SetEmailAddressRequestDataParams> { t ->
                JSONObject().apply {
                    put(EMAIL_ADDRESS_FIELD, t.emailAddress)
                }
            }
        }
    }

    data class SetCustomerFirstName(
        val params: SetCustomerFirstNameRequestDataParams
    ) : Action() {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetCustomerFirstName> { t ->
                JSONObject().apply {
                    put(TYPE_FIELD, "SET_CUSTOMER_FIRST_NAME")
                    put(
                        PARAMS_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<SetCustomerFirstNameRequestDataParams>()
                            .serialize(t.params)
                    )
                }
            }
        }
    }

    data class SetCustomerFirstNameRequestDataParams(
        val firstName: String
    ) : JSONObjectSerializable {
        companion object {
            private const val FIRST_NAME_FIELD = "firstName"

            @JvmField
            val serializer = JSONObjectSerializer<SetCustomerFirstNameRequestDataParams> { t ->
                JSONObject().apply {
                    put(FIRST_NAME_FIELD, t.firstName)
                }
            }
        }
    }

    data class SetCustomerLastName(
        val params: SetCustomerLastNameRequestDataParams
    ) : Action() {
        companion object {
            @JvmField
            val serializer = JSONObjectSerializer<SetCustomerLastName> { t ->
                JSONObject().apply {
                    put(TYPE_FIELD, "SET_CUSTOMER_LAST_NAME")
                    put(
                        PARAMS_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<SetCustomerLastNameRequestDataParams>()
                            .serialize(t.params)
                    )
                }
            }
        }
    }

    data class SetCustomerLastNameRequestDataParams(
        val lastName: String
    ) : JSONObjectSerializable {
        companion object {
            private const val LAST_NAME_FIELD = "lastName"

            @JvmField
            val serializer = JSONObjectSerializer<SetCustomerLastNameRequestDataParams> { t ->
                JSONObject().apply {
                    put(LAST_NAME_FIELD, t.lastName)
                }
            }
        }
    }

    data class SetPaymentMethod(val params: SetPaymentMethodRequestDataParams) : Action() {
        companion object {

            @JvmField
            val serializer = JSONObjectSerializer<SetPaymentMethod> { t ->
                JSONObject().apply {
                    put(TYPE_FIELD, "SELECT_PAYMENT_METHOD")
                    put(
                        PARAMS_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<SetPaymentMethodRequestDataParams>()
                            .serialize(t.params)
                    )
                }
            }
        }
    }

    data class SetPaymentMethodRequestDataParams(
        val paymentMethodType: String,
        val binData: BinData? = null
    ) : JSONObjectSerializable {
        companion object {

            private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
            private const val BIN_DATA_FIELD = "binData"

            @JvmField
            val serializer = JSONObjectSerializer<SetPaymentMethodRequestDataParams> { t ->
                JSONObject().apply {
                    put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    putOpt(
                        BIN_DATA_FIELD,
                        t.binData?.let {
                            JSONSerializationUtils.getJsonObjectSerializer<BinData>()
                                .serialize(t.binData)
                        }
                    )
                }
            }
        }
    }

    data class UnsetPaymentMethod(val params: Unit = Unit) : Action() {
        companion object {

            @JvmField
            val serializer = JSONObjectSerializer<UnsetPaymentMethod> {
                JSONObject().apply {
                    put(TYPE_FIELD, "UNSELECT_PAYMENT_METHOD")
                    put(
                        PARAMS_FIELD,
                        JSONSerializationUtils.getJsonObjectSerializer<EmptyDataRequest>()
                            .serialize(EmptyDataRequest())
                    )
                }
            }
        }
    }

    class SetBillingAddress(
        val params: SetBillingAddressRequestDataParams
    ) : Action() {
        companion object {

            @JvmField
            val serializer = JSONObjectSerializer<SetBillingAddress> { t ->
                JSONObject().apply {
                    put(TYPE_FIELD, "SET_BILLING_ADDRESS")
                    put(
                        PARAMS_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<SetBillingAddressRequestDataParams>()
                            .serialize(t.params)
                    )
                }
            }
        }
    }

    data class SetBillingAddressRequestDataParams(
        val billingAddress: Address
    ) : JSONObjectSerializable {

        companion object {

            private const val BILLING_ADDRESS_FIELD = "billingAddress"

            @JvmField
            val serializer = JSONObjectSerializer<SetBillingAddressRequestDataParams> { t ->
                JSONObject().apply {
                    put(
                        BILLING_ADDRESS_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<Address>()
                            .serialize(t.billingAddress)
                    )
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
        val serializer = JSONObjectSerializer<ClientSessionActionsDataRequest> { t ->
            JSONObject().apply {
                put(
                    ACTIONS_FIELD,
                    JSONArray().apply {
                        t.actions.map { action ->
                            put(
                                when (action) {
                                    is SetPaymentMethod ->
                                        JSONSerializationUtils
                                            .getJsonObjectSerializer<SetPaymentMethod>()
                                            .serialize(action)

                                    is UnsetPaymentMethod ->
                                        JSONSerializationUtils
                                            .getJsonObjectSerializer<UnsetPaymentMethod>()
                                            .serialize(action)

                                    is SetBillingAddress ->
                                        JSONSerializationUtils
                                            .getJsonObjectSerializer<SetBillingAddress>()
                                            .serialize(action)

                                    is SetEmailAddress ->
                                        JSONSerializationUtils
                                            .getJsonObjectSerializer<SetEmailAddress>()
                                            .serialize(action)

                                    is SetCustomerFirstName ->
                                        JSONSerializationUtils
                                            .getJsonObjectSerializer<SetCustomerFirstName>()
                                            .serialize(action)

                                    is SetCustomerLastName ->
                                        JSONSerializationUtils
                                            .getJsonObjectSerializer<SetCustomerLastName>()
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

internal fun BaseActionUpdateParams.toActionData() = when (this) {
    is ActionUpdateSelectPaymentMethodParams -> listOf(
        ClientSessionActionsDataRequest.SetPaymentMethod(
            ClientSessionActionsDataRequest.SetPaymentMethodRequestDataParams(
                paymentMethodType,
                cardNetwork?.let { BinData(it) }
            )
        )
    )

    is ActionUpdateUnselectPaymentMethodParams ->
        listOf(ClientSessionActionsDataRequest.UnsetPaymentMethod())

    is ActionUpdateBillingAddressParams -> listOf(
        ClientSessionActionsDataRequest.SetBillingAddress(
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
    )

    is ActionUpdateCustomerDetailsParams -> buildList {
        firstName?.let {
            add(
                ClientSessionActionsDataRequest.SetCustomerFirstName(
                    ClientSessionActionsDataRequest.SetCustomerFirstNameRequestDataParams(it)
                )
            )
        }
        lastName?.let {
            add(
                ClientSessionActionsDataRequest.SetCustomerLastName(
                    ClientSessionActionsDataRequest.SetCustomerLastNameRequestDataParams(it)
                )
            )
        }
        emailAddress?.let {
            add(
                ClientSessionActionsDataRequest.SetEmailAddress(
                    ClientSessionActionsDataRequest.SetEmailAddressRequestDataParams(it)
                )
            )
        }
    }
}
