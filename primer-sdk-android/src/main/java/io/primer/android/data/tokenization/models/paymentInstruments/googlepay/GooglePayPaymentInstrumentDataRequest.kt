package io.primer.android.data.tokenization.models.paymentInstruments.googlepay

import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.data.tokenization.models.PaymentInstrumentDataRequest
import io.primer.android.domain.tokenization.models.paymentInstruments.googlepay.GooglePayFlow
import org.json.JSONObject

internal data class GooglePayPaymentInstrumentDataRequest(
    val merchantId: String,
    val encryptedPayload: String,
    val flow: GooglePayFlow
) : PaymentInstrumentDataRequest() {
    companion object {

        private const val MERCHANT_ID_FIELD = "merchantId"
        private const val ENCRYPTED_PAYLOAD_FIELD = "encryptedPayload"
        private const val FLOW_FIELD = "flow"

        @JvmField
        val serializer =
            object : JSONSerializer<GooglePayPaymentInstrumentDataRequest> {
                override fun serialize(t: GooglePayPaymentInstrumentDataRequest): JSONObject {
                    return JSONObject().apply {
                        put(MERCHANT_ID_FIELD, t.merchantId)
                        put(ENCRYPTED_PAYLOAD_FIELD, t.encryptedPayload)
                        put(FLOW_FIELD, t.flow.name)
                    }
                }
            }
    }
}
