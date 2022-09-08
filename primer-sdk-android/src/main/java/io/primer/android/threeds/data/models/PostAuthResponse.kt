package io.primer.android.threeds.data.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import org.json.JSONObject

internal data class PostAuthResponse(
    val token: PaymentMethodTokenInternal,
    val authentication: AuthenticationDataResponse?,
    val resumeToken: String?,
) : JSONDeserializable {
    companion object {
        private const val TOKEN_FIELD = "token"
        private const val AUTHENTICATION_FIELD = "authentication"
        private const val RESUME_TOKEN_FIELD = "resumeToken"

        @JvmField
        val deserializer = object : JSONDeserializer<PostAuthResponse> {

            override fun deserialize(t: JSONObject): PostAuthResponse {
                return PostAuthResponse(
                    JSONSerializationUtils.getDeserializer<PaymentMethodTokenInternal>()
                        .deserialize(
                            t.getJSONObject(
                                TOKEN_FIELD
                            )
                        ),
                    t.optJSONObject(AUTHENTICATION_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<AuthenticationDataResponse>()
                            .deserialize(
                                it
                            )
                    },
                    t.optNullableString(RESUME_TOKEN_FIELD)
                )
            }
        }
    }
}
