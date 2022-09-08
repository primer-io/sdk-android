package io.primer.android.data.payments.create.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject

internal data class CreatePaymentDataRequest(private val paymentMethodToken: String) :
    JSONSerializable {

    companion object {

        private const val PAYMENT_METHOD_TOKEN_FILED = "paymentMethodToken"

        @JvmField
        val serializer = object : JSONSerializer<CreatePaymentDataRequest> {
            override fun serialize(t: CreatePaymentDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_TOKEN_FILED, t.paymentMethodToken)
                }
            }
        }
    }
}
