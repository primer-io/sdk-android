package io.primer.android.data.payments.resume.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ResumePaymentDataRequest(val resumeToken: String) : JSONObjectSerializable {
    companion object {

        private const val RESUME_TOKEN_FIELD = "resumeToken"

        @JvmField
        val serializer = JSONObjectSerializer<ResumePaymentDataRequest> { t ->
            JSONObject().apply {
                put(RESUME_TOKEN_FIELD, t.resumeToken)
            }
        }
    }
}
