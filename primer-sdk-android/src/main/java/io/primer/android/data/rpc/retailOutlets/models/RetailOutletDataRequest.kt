package io.primer.android.data.rpc.retailOutlets.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutlet
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutletParams
import org.json.JSONObject

internal data class RetailOutletDataRequest(
    val paymentMethodConfigId: String,
) : JSONSerializable {
    companion object {

        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"

        @JvmField
        val serializer = object : JSONSerializer<RetailOutletDataRequest> {
            override fun serialize(t: RetailOutletDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                }
            }
        }
    }
}

internal fun RetailOutletParams.toRetailOutletRequest() = RetailOutletDataRequest(
    paymentMethodConfigId,
)

internal fun RetailOutletDataResponse.toRetailOutlet() = RetailOutlet(id, name, disabled, iconUrl)
