package io.primer.android.data.tokenization.models.paymentInstruments.async.bancontactCard

import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONSerializer
import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.data.tokenization.models.paymentInstruments.async.AsyncPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.BaseSessionInfoDataRequest
import org.json.JSONObject

internal data class AdyenBancontactCardPaymentInstrumentDataRequest(
    val number: String,
    val expirationMonth: String,
    val expirationYear: String,
    val cardholderName: String,
    override val paymentMethodType: String,
    override val paymentMethodConfigId: String,
    override val sessionInfo: AdyenBancontactSessionInfoDataRequest,
    override val type: PaymentInstrumentType
) : AsyncPaymentInstrumentDataRequest(
    paymentMethodType,
    paymentMethodConfigId,
    sessionInfo,
    type,
) {

    companion object {

        private const val NUMBER_FIELD = "number"
        private const val EXPIRATION_MONTH_FIELD = "expirationMonth"
        private const val EXPIRATION_YEAR_FIELD = "expirationYear"
        private const val CARDHOLDER_NAME_FIELD = "cardholderName"
        private const val TYPE_FIELD = "type"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val SESSION_INFO_FIELD = "sessionInfo"

        @JvmField
        val serializer =
            object : JSONSerializer<AdyenBancontactCardPaymentInstrumentDataRequest> {
                override fun serialize(t: AdyenBancontactCardPaymentInstrumentDataRequest):
                    JSONObject {
                    return JSONObject().apply {
                        put(TYPE_FIELD, t.type.name)
                        put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                        put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                        put(
                            SESSION_INFO_FIELD,
                            JSONSerializationUtils
                                .getSerializer<BaseSessionInfoDataRequest>()
                                .serialize(t.sessionInfo)
                        )
                        put(NUMBER_FIELD, t.number)
                        put(EXPIRATION_MONTH_FIELD, t.expirationMonth)
                        put(EXPIRATION_YEAR_FIELD, t.expirationYear)
                        put(CARDHOLDER_NAME_FIELD, t.cardholderName)
                    }
                }
            }
    }
}
