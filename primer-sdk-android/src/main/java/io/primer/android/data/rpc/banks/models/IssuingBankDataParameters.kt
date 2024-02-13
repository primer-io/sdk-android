package io.primer.android.data.rpc.banks.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class IssuingBankDataParameters(
    val paymentMethod: String,
    val locale: String
) : JSONObjectSerializable {

    companion object {
        const val PAYMENT_METHOD_FIELD = "paymentMethod"
        const val LOCALE_FIELD = "locale"

        @JvmField
        val serializer = JSONObjectSerializer<IssuingBankDataParameters> { t ->
            JSONObject().apply {
                put(PAYMENT_METHOD_FIELD, t.paymentMethod)
                put(LOCALE_FIELD, t.locale)
            }
        }
    }
}
