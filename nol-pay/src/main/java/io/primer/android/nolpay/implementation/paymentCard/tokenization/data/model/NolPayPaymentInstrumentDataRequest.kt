package io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model

import io.primer.android.configuration.data.model.PaymentInstrumentType
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest
import org.json.JSONObject

internal data class NolPayPaymentInstrumentDataRequest(
    val paymentMethodType: String,
    val paymentMethodConfigId: String,
    val sessionInfo: NolPaySessionInfoDataRequest,
    val type: PaymentInstrumentType,
) : BasePaymentInstrumentDataRequest {
    companion object {
        private const val TYPE_FIELD = "type"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val SESSION_INFO_FIELD = "sessionInfo"

        @JvmField
        val serializer =
            JSONObjectSerializer<NolPayPaymentInstrumentDataRequest> { t ->
                JSONObject().apply {
                    putOpt(TYPE_FIELD, t.type.name)
                    put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(
                        SESSION_INFO_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<NolPaySessionInfoDataRequest>()
                            .serialize(t.sessionInfo),
                    )
                }
            }
    }
}
