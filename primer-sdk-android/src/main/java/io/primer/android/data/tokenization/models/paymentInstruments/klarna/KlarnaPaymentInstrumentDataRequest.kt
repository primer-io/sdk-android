package io.primer.android.data.tokenization.models.paymentInstruments.klarna

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.PaymentInstrumentDataRequest
import org.json.JSONObject

internal data class KlarnaPaymentInstrumentDataRequest(
    val klarnaCustomerToken: String?,
    val sessionData: CreateCustomerTokenDataResponse.SessionData
) : PaymentInstrumentDataRequest() {
    companion object {

        private const val KLARNA_CUSTOMER_TOKEN_FIELD = "klarnaCustomerToken"
        private const val SESSION_DATA_FIELD = "sessionData"

        @JvmField
        val serializer =
            object : JSONObjectSerializer<KlarnaPaymentInstrumentDataRequest> {
                override fun serialize(t: KlarnaPaymentInstrumentDataRequest): JSONObject {
                    return JSONObject().apply {
                        putOpt(KLARNA_CUSTOMER_TOKEN_FIELD, t.klarnaCustomerToken)
                        put(
                            SESSION_DATA_FIELD,
                            JSONSerializationUtils
                                .getJsonObjectSerializer<
                                    CreateCustomerTokenDataResponse.SessionData>()
                                .serialize(t.sessionData)
                        )
                    }
                }
            }
    }
}
