package io.primer.android.data.tokenization.models.paymentInstruments.paypal

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ExternalPayerInfoRequest(
    val email: String?
) : JSONObjectSerializable {
    companion object {

        private const val EMAIL_FIELD = "email"

        @JvmField
        val serializer = object : JSONObjectSerializer<ExternalPayerInfoRequest> {
            override fun serialize(t: ExternalPayerInfoRequest): JSONObject {
                return JSONObject().apply {
                    put(EMAIL_FIELD, t.email)
                }
            }
        }
    }
}
