package io.primer.android.payments.core.create.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class CreatePaymentDataRequest(private val paymentMethodToken: String) :
    JSONObjectSerializable {

    companion object {

        private const val PAYMENT_METHOD_TOKEN_FILED = "paymentMethodToken"

        @JvmField
        val serializer = JSONObjectSerializer<CreatePaymentDataRequest> { t ->
            JSONObject().apply {
                put(PAYMENT_METHOD_TOKEN_FILED, t.paymentMethodToken)
            }
        }
    }
}
