package io.primer.android.data.tokenization.models.paymentInstruments.card

import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.data.tokenization.models.PaymentInstrumentDataRequest
import org.json.JSONObject

internal data class CardPaymentInstrumentDataRequest(
    val number: String,
    val expirationMonth: String,
    val expirationYear: String,
    val cvv: String,
    val cardholderName: String?
) : PaymentInstrumentDataRequest() {
    companion object {

        private const val NUMBER_FIELD = "number"
        private const val EXPIRATION_MONTH_FIELD = "expirationMonth"
        private const val EXPIRATION_YEAR_FIELD = "expirationYear"
        private const val CVV_FIELD = "cvv"
        private const val CARDHOLDER_NAME_FIELD = "cardholderName"

        @JvmField
        val serializer =
            object : JSONSerializer<CardPaymentInstrumentDataRequest> {
                override fun serialize(t: CardPaymentInstrumentDataRequest): JSONObject {
                    return JSONObject().apply {
                        put(NUMBER_FIELD, t.number)
                        put(EXPIRATION_MONTH_FIELD, t.expirationMonth)
                        put(EXPIRATION_YEAR_FIELD, t.expirationYear)
                        put(CVV_FIELD, t.cvv)
                        putOpt(CARDHOLDER_NAME_FIELD, t.cardholderName)
                    }
                }
            }
    }
}
