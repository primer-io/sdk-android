package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class ExternalPayerInfoRequest(
    val email: String?,
    val externalPayerId: String?,
    val firstName: String?,
    val lastName: String?,
) : JSONObjectSerializable {
    companion object {
        private const val EMAIL_FIELD = "email"
        private const val EXTERNAL_PAYER_ID_FIELD = "external_payer_id" // BE does not support camelCase
        private const val FIRST_NAME_FIELD = "first_name" // BE does not support camelCase
        private const val LAST_NAME_FIELD = "last_name" // BE does not support camelCase

        @JvmField
        val serializer =
            JSONObjectSerializer<ExternalPayerInfoRequest> { t ->
                JSONObject().apply {
                    put(EMAIL_FIELD, t.email)
                    put(EXTERNAL_PAYER_ID_FIELD, t.externalPayerId)
                    put(FIRST_NAME_FIELD, t.firstName)
                    put(LAST_NAME_FIELD, t.lastName)
                }
            }
    }
}
