package io.primer.android.data.payments.methods.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.sequence
import org.json.JSONObject

internal data class PaymentMethodTokenInternalResponse(
    val data: List<PaymentMethodVaultTokenInternal>
) : JSONDeserializable {
    companion object {
        private const val DATA_FIELD = "data"

        @JvmField
        val deserializer = object : JSONDeserializer<PaymentMethodTokenInternalResponse> {
            override fun deserialize(t: JSONObject): PaymentMethodTokenInternalResponse {
                return PaymentMethodTokenInternalResponse(
                    t.getJSONArray(DATA_FIELD).sequence<JSONObject>().map {
                        JSONSerializationUtils
                            .getDeserializer<PaymentMethodVaultTokenInternal>()
                            .deserialize(it)
                    }.toList()
                )
            }
        }
    }
}
