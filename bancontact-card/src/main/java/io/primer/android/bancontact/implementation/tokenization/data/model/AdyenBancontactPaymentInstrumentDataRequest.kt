package io.primer.android.bancontact.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest
import org.json.JSONObject

internal data class AdyenBancontactPaymentInstrumentDataRequest(
    val number: String,
    val expirationMonth: String,
    val expirationYear: String,
    val cardholderName: String,
    val paymentMethodType: String,
    val paymentMethodConfigId: String,
    val sessionInfo: AdyenBancontactSessionInfoDataRequest,
    val type: PaymentInstrumentType,
    val authenticationProvider: String? = null
) : BasePaymentInstrumentDataRequest {

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
            JSONObjectSerializer<AdyenBancontactPaymentInstrumentDataRequest> { t ->
                JSONObject().apply {
                    put(TYPE_FIELD, t.type.name)
                    put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(
                        SESSION_INFO_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<AdyenBancontactSessionInfoDataRequest>()
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
