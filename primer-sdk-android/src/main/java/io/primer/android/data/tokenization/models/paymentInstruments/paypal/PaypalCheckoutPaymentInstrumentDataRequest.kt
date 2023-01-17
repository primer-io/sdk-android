package io.primer.android.data.tokenization.models.paymentInstruments.paypal

import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.data.tokenization.models.PaymentInstrumentDataRequest
import org.json.JSONObject

internal data class PaypalCheckoutPaymentInstrumentDataRequest(
    val paypalOrderId: String?,
    val externalPayerInfo: ExternalPayerInfoRequest?
) : PaymentInstrumentDataRequest() {
    companion object {

        private const val PAYPAL_ORDER_ID_FIELD = "paypalOrderId"
        private const val EXTERNAL_PAYER_INFO_FIELD = "externalPayerInfo"

        @JvmField
        val serializer =
            object : JSONSerializer<PaypalCheckoutPaymentInstrumentDataRequest> {
                override fun serialize(t: PaypalCheckoutPaymentInstrumentDataRequest):
                    JSONObject {
                    return JSONObject().apply {
                        putOpt(PAYPAL_ORDER_ID_FIELD, t.paypalOrderId)
                        put(
                            EXTERNAL_PAYER_INFO_FIELD,
                            t.externalPayerInfo?.let {
                                JSONSerializationUtils
                                    .getSerializer<ExternalPayerInfoRequest>()
                                    .serialize(it)
                            }
                        )
                    }
                }
            }
    }
}
