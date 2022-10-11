package io.primer.android.data.rpc.retail_outlets.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject

internal data class RetailOutletDataParameters(
    val paymentMethod: String,
    val locale: String
) : JSONSerializable {

    companion object {
        private const val PAYMENT_METHOD_FIELD = "paymentMethod"
        private const val LOCALE_FIELD = "locale"

        @JvmField
        val serializer = object : JSONSerializer<RetailOutletDataParameters> {
            override fun serialize(t: RetailOutletDataParameters): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_FIELD, t.paymentMethod)
                    put(LOCALE_FIELD, t.locale)
                }
            }
        }
    }
}
