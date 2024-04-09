package io.primer.android.data.tokenization.models.paymentInstruments.paypal

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ExternalPayerInfoRequest(
    val email: String?,
    val externalPayerId: String?
) : JSONObjectSerializable {
    companion object {

        private const val EMAIL_FIELD = "email"
        private const val EXTERNAL_PAYER_ID = "external_payer_id" // BE does not support camelCase

        @JvmField
        val serializer = JSONObjectSerializer<ExternalPayerInfoRequest> { t ->
            JSONObject().apply {
                put(EMAIL_FIELD, t.email)
                put(EXTERNAL_PAYER_ID, t.externalPayerId)
            }
        }
    }
}
