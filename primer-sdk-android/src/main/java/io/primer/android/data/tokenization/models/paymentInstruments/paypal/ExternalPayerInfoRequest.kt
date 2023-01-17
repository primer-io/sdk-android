package io.primer.android.data.tokenization.models.paymentInstruments.paypal

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject

internal data class ExternalPayerInfoRequest(
    val email: String?
) : JSONSerializable {
    companion object {

        private const val EMAIL_FIELD = "email"

        @JvmField
        val serializer = object : JSONSerializer<ExternalPayerInfoRequest> {
            override fun serialize(t: ExternalPayerInfoRequest): JSONObject {
                return JSONObject().apply {
                    put(EMAIL_FIELD, t.email)
                }
            }
        }
    }
}
