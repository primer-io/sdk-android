package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import org.json.JSONObject

internal data class CreateSessionDataResponse(
    val clientToken: String,
    val sessionId: String
) : JSONDeserializable {

    companion object {
        private const val CLIENT_TOKEN_FIELD = "clientToken"
        private const val SESSION_ID_FIELD = "sessionId"

        @JvmField
        val deserializer = object : JSONDeserializer<CreateSessionDataResponse> {

            override fun deserialize(t: JSONObject): CreateSessionDataResponse {
                return CreateSessionDataResponse(
                    t.getString(CLIENT_TOKEN_FIELD),
                    t.getString(SESSION_ID_FIELD)
                )
            }
        }
    }
}

internal fun CreateSessionDataResponse.toKlarnaSession(webViewTitle: String?) = KlarnaSession(
    webViewTitle.orEmpty(),
    sessionId,
    clientToken
)
