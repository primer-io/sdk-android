package io.primer.android.data.rpc.retail_outlets.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.data.rpc.RpcFunction
import io.primer.android.domain.rpc.retail_outlets.models.RetailOutlet
import io.primer.android.domain.rpc.retail_outlets.models.RetailOutletParams
import org.json.JSONObject

internal data class RetailOutletDataRequest(
    val paymentMethodConfigId: String,
    val command: String,
    val parameters: RetailOutletDataParameters
) : JSONSerializable {
    companion object {

        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val COMMAND_FIELD = "command"
        private const val PARAMETERS_FIELD = "parameters"

        @JvmField
        val serializer = object : JSONSerializer<RetailOutletDataRequest> {
            override fun serialize(t: RetailOutletDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(COMMAND_FIELD, t.command)
                    put(
                        PARAMETERS_FIELD,
                        JSONSerializationUtils.getSerializer<RetailOutletDataParameters>()
                            .serialize(t.parameters)
                    )
                }
            }
        }
    }
}

internal fun RetailOutletParams.toRetailOutletRequest() = RetailOutletDataRequest(
    paymentMethodConfigId,
    RpcFunction.FETCH_RETAIL_OUTLETS.name,
    RetailOutletDataParameters(
        selectedRetailOutlet,
        locale.toLanguageTag(),
    )
)

internal fun RetailOutletDataResponse.toRetailOutlet() = RetailOutlet(id, name, disabled, iconUrl)
