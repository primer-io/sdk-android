package io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.domain.payments.apaya.models.ApayaSession
import org.json.JSONObject

internal data class CreateSessionDataResponse(
    val url: String,
    val token: String
) : JSONDeserializable {

    companion object {
        private const val URL_FIELD = "url"
        private const val TOKEN_FIELD = "token"

        @JvmField
        val deserializer = object : JSONDeserializer<CreateSessionDataResponse> {

            override fun deserialize(t: JSONObject): CreateSessionDataResponse {
                return CreateSessionDataResponse(t.getString(URL_FIELD), t.getString(TOKEN_FIELD))
            }
        }
    }
}

internal fun CreateSessionDataResponse.toApayaSession(webViewTitle: String?) = ApayaSession(
    webViewTitle,
    url,
    token
)
