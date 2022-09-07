package io.primer.android.data.payments.resume.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject

internal data class ResumePaymentDataRequest(val resumeToken: String) : JSONSerializable {
    companion object {

        private const val RESUME_TOKEN_FIELD = "resumeToken"

        @JvmField
        val serializer = object : JSONSerializer<ResumePaymentDataRequest> {
            override fun serialize(t: ResumePaymentDataRequest): JSONObject {
                return JSONObject().apply {
                    put(RESUME_TOKEN_FIELD, t.resumeToken)
                }
            }
        }
    }
}
