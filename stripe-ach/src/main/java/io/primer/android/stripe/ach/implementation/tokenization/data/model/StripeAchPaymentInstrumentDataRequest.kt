package io.primer.android.stripe.ach.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest
import org.json.JSONObject

internal data class StripeAchPaymentInstrumentDataRequest(
    val paymentMethodType: String,
    val paymentMethodConfigId: String,
    val sessionInfo: StripeAchSessionInfoDataRequest,
    val type: PaymentInstrumentType
) : BasePaymentInstrumentDataRequest {
    companion object {

        private const val TYPE_FIELD = "type"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val SESSION_INFO_FIELD = "sessionInfo"
        private const val AUTHENTICATION_PROVIDER_FIELD = "authenticationProvider"

        @JvmField
        val serializer =
            JSONObjectSerializer<StripeAchPaymentInstrumentDataRequest> { t ->
                JSONObject().apply {
                    putOpt(TYPE_FIELD, t.type.name)
                    put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(
                        SESSION_INFO_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<StripeAchSessionInfoDataRequest>()
                            .serialize(t.sessionInfo)
                    )
                    put(AUTHENTICATION_PROVIDER_FIELD, "STRIPE")
                }
            }
    }
}
