package io.primer.android.data.payments.methods.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.sequence
import org.json.JSONObject

internal data class PaymentMethodTokenInternalResponse(
    val data: List<PaymentMethodVaultTokenInternal>
) : JSONDeserializable {
    companion object {
        private const val DATA_FIELD = "data"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            PaymentMethodTokenInternalResponse(
                t.getJSONArray(DATA_FIELD).sequence<JSONObject>().map {
                    JSONSerializationUtils
                        .getJsonObjectDeserializer<PaymentMethodVaultTokenInternal>()
                        .deserialize(it)
                }.toList()
            )
        }
    }
}
