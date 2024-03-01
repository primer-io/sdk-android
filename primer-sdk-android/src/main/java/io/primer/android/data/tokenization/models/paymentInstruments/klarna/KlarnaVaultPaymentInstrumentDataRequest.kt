package io.primer.android.data.tokenization.models.paymentInstruments.klarna

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.KlarnaSessionData
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.data.tokenization.models.PaymentInstrumentDataRequest
import org.json.JSONObject

internal data class KlarnaVaultPaymentInstrumentDataRequest(
    val klarnaCustomerToken: String?,
    val sessionData: KlarnaSessionData
) : PaymentInstrumentDataRequest() {
    companion object {

        private const val KLARNA_CUSTOMER_TOKEN_FIELD = "klarnaCustomerToken"
        private const val SESSION_DATA_FIELD = "sessionData"

        @JvmField
        val serializer =
            object : JSONObjectSerializer<KlarnaVaultPaymentInstrumentDataRequest> {
                override fun serialize(t: KlarnaVaultPaymentInstrumentDataRequest): JSONObject {
                    return JSONObject().apply {
                        putOpt(KLARNA_CUSTOMER_TOKEN_FIELD, t.klarnaCustomerToken)
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
