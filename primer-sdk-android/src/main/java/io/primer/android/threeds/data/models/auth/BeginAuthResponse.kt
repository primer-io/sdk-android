package io.primer.android.threeds.data.models.auth

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.threeds.data.models.common.AuthenticationDataResponse
import org.json.JSONObject

internal data class BeginAuthResponse(
    val token: PaymentMethodTokenInternal,
    val authentication: AuthenticationDataResponse,
    val resumeToken: String
) : JSONDeserializable {

    companion object {
        private const val TOKEN_FIELD = "token"
        private const val AUTHENTICATION_FIELD = "authentication"
        private const val RESUME_TOKEN_FIELD = "resumeToken"

        @JvmField
        val deserializer = object : JSONObjectDeserializer<BeginAuthResponse> {

            override fun deserialize(t: JSONObject): BeginAuthResponse {
                return BeginAuthResponse(
                    JSONSerializationUtils.getJsonObjectDeserializer<PaymentMethodTokenInternal>()
                        .deserialize(
                            t.getJSONObject(
                                TOKEN_FIELD
                            )
                        ),
                    JSONSerializationUtils.getJsonObjectDeserializer<AuthenticationDataResponse>()
                        .deserialize(
                            t.getJSONObject(
                                AUTHENTICATION_FIELD
                            )
                        ),
                    t.getString(RESUME_TOKEN_FIELD)
                )
            }
        }
    }
}
