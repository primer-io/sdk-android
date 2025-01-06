package io.primer.android.klarna.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.klarna.implementation.session.data.models.KlarnaSessionData
import org.json.JSONObject

internal data class KlarnaCheckoutPaymentInstrumentDataRequest(
    val klarnaAuthorizationToken: String?,
    val sessionData: KlarnaSessionData,
) : KlarnaPaymentInstrumentDataRequest {
    companion object {
        private const val KLARNA_AUTHORIZATION_TOKEN_FIELD = "klarnaAuthorizationToken"
        private const val SESSION_DATA_FIELD = "sessionData"

        @JvmField
        val serializer =
            JSONObjectSerializer<KlarnaCheckoutPaymentInstrumentDataRequest> { t ->
                JSONObject().apply {
                    putOpt(KLARNA_AUTHORIZATION_TOKEN_FIELD, t.klarnaAuthorizationToken)
                    put(
                        SESSION_DATA_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<KlarnaSessionData>()
                            .serialize(t.sessionData),
                    )
                }
            }
    }
}
