package io.primer.android.data.rpc.retailOutlets.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutlet
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutletParams
import org.json.JSONObject

internal data class RetailOutletDataRequest(
    val paymentMethodConfigId: String
) : JSONObjectSerializable {
    companion object {

        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"

        @JvmField
        val serializer = JSONObjectSerializer<RetailOutletDataRequest> { t ->
            JSONObject().apply {
                put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
            }
        }
    }
}

internal fun RetailOutletParams.toRetailOutletRequest() = RetailOutletDataRequest(
    paymentMethodConfigId
)

internal fun RetailOutletDataResponse.toRetailOutlet() = RetailOutlet(id, name, disabled, iconUrl)
