package io.primer.android.threeds.data.models.auth

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.threeds.data.models.common.AuthenticationDataResponse

internal data class BeginAuthResponse(
    val token: PaymentMethodTokenInternal,
    val authentication: AuthenticationDataResponse,
    val resumeToken: String,
) : JSONDeserializable {
    companion object {
        private const val TOKEN_FIELD = "token"
        private const val AUTHENTICATION_FIELD = "authentication"
        private const val RESUME_TOKEN_FIELD = "resumeToken"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                BeginAuthResponse(
                    token =
                    JSONSerializationUtils.getJsonObjectDeserializer<PaymentMethodTokenInternal>()
                        .deserialize(
                            t.getJSONObject(
                                TOKEN_FIELD,
                            ),
                        ),
                    authentication =
                    JSONSerializationUtils.getJsonObjectDeserializer<AuthenticationDataResponse>()
                        .deserialize(
                            t.getJSONObject(
                                AUTHENTICATION_FIELD,
                            ),
                        ),
                    resumeToken = t.getString(RESUME_TOKEN_FIELD),
                )
            }
    }
}
