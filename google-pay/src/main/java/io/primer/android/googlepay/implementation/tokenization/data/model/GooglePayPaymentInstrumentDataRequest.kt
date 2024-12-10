package io.primer.android.googlepay.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.googlepay.implementation.tokenization.domain.model.GooglePayFlow
import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest
import org.json.JSONObject

internal data class GooglePayPaymentInstrumentDataRequest(
    val merchantId: String,
    val encryptedPayload: String,
    val flow: GooglePayFlow
) : BasePaymentInstrumentDataRequest {
    companion object {

        internal const val MERCHANT_ID_FIELD = "merchantId"
        internal const val ENCRYPTED_PAYLOAD_FIELD = "encryptedPayload"
        internal const val FLOW_FIELD = "flow"

        @JvmField
        val serializer = JSONObjectSerializer<GooglePayPaymentInstrumentDataRequest> { t ->
            JSONObject().apply {
                put(MERCHANT_ID_FIELD, t.merchantId)
                put(ENCRYPTED_PAYLOAD_FIELD, t.encryptedPayload)
                put(FLOW_FIELD, t.flow.name)
            }
        }
    }
}
