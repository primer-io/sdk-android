package io.primer.android.stripe.ach.implementation.payment.confirmation.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class StripeAchCompletePaymentDataRequest(
    val mandateTimestamp: String,
    val paymentMethodId: String?,
) : JSONObjectSerializable {
    companion object {
        private const val MANDATE_TIMESTAMP_FIELD = "mandateSignatureTimestamp"
        private const val PAYMENT_METHOD_ID_FIELD = "paymentMethodId"

        @JvmField
        val serializer =
            JSONObjectSerializer<StripeAchCompletePaymentDataRequest> { t ->
                JSONObject().apply {
                    put(MANDATE_TIMESTAMP_FIELD, t.mandateTimestamp)
                    put(PAYMENT_METHOD_ID_FIELD, t.paymentMethodId)
                }
            }
    }
}
