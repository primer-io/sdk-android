package io.primer.android.data.tokenization.models.paymentInstruments.nolpay

import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.PaymentInstrumentDataRequest
import org.json.JSONObject

internal class NolPayPaymentInstrumentDataRequest(
    private val mobileNumber: String,
    private val sdkId: String,
    private val cardNumber: String,
) : PaymentInstrumentDataRequest() {
    companion object {

        private const val SDK_ID_FIELD = "sdkId"
        private const val MOBILE_NUMBER_FIELD = "mobileNumber"
        private const val CARD_NUMBER_FIELD = "cardNumber"

        @JvmField
        val serializer =
            object : JSONObjectSerializer<NolPayPaymentInstrumentDataRequest> {
                override fun serialize(t: NolPayPaymentInstrumentDataRequest): JSONObject {
                    return JSONObject().apply {
                        put(SDK_ID_FIELD, t.sdkId)
                        put(MOBILE_NUMBER_FIELD, t.mobileNumber)
                        put(CARD_NUMBER_FIELD, t.cardNumber)
                    }
                }
            }
    }
}
