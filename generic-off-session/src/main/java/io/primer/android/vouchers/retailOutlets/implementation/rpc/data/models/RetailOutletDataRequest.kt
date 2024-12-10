package io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutlet
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

internal fun RetailOutletDataResponse.toRetailOutlet() = RetailOutlet(id, name, disabled, iconUrl)
