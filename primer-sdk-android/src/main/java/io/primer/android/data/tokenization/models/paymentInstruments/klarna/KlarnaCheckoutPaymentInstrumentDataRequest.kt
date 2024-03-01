package io.primer.android.data.tokenization.models.paymentInstruments.klarna

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.KlarnaSessionData
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.data.tokenization.models.PaymentInstrumentDataRequest
import org.json.JSONObject

internal data class KlarnaCheckoutPaymentInstrumentDataRequest(
    val klarnaAuthorizationToken: String?,
    val sessionData: KlarnaSessionData
) : PaymentInstrumentDataRequest() {
    companion object {

        private const val KLARNA_AUTHORIZATION_TOKEN_FIELD = "klarnaAuthorizationToken"
        private const val SESSION_DATA_FIELD = "sessionData"

        @JvmField
        val serializer =
            object : JSONObjectSerializer<KlarnaCheckoutPaymentInstrumentDataRequest> {
                override fun serialize(t: KlarnaCheckoutPaymentInstrumentDataRequest): JSONObject {
                    return JSONObject().apply {
                        putOpt(KLARNA_AUTHORIZATION_TOKEN_FIELD, t.klarnaAuthorizationToken)
                        put(
                            SESSION_DATA_FIELD,
                            JSONSerializationUtils
                                .getJsonObjectSerializer<KlarnaSessionData>()
                                .serialize(t.sessionData)
                        )
                    }
                }
            }
    }
}
