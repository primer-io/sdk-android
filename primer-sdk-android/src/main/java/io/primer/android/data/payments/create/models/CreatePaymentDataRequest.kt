package io.primer.android.data.payments.create.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class CreatePaymentDataRequest(private val paymentMethodToken: String) :
    JSONObjectSerializable {

    companion object {

        private const val PAYMENT_METHOD_TOKEN_FILED = "paymentMethodToken"

        @JvmField
        val serializer = object : JSONObjectSerializer<CreatePaymentDataRequest> {
            override fun serialize(t: CreatePaymentDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_TOKEN_FILED, t.paymentMethodToken)
                }
            }
        }
    }
}
