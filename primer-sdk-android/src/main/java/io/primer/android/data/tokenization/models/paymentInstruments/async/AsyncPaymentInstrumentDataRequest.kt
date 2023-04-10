package io.primer.android.data.tokenization.models.paymentInstruments.async

import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.data.tokenization.models.PaymentInstrumentDataRequest
import org.json.JSONObject

internal open class AsyncPaymentInstrumentDataRequest(
    open val paymentMethodType: String,
    open val paymentMethodConfigId: String,
    open val sessionInfo: BaseSessionInfoDataRequest,
    open val type: PaymentInstrumentType,
) : PaymentInstrumentDataRequest() {
    companion object {

        private const val TYPE_FIELD = "type"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val SESSION_INFO_FIELD = "sessionInfo"

        @JvmField
        val serializer =
            object : JSONObjectSerializer<AsyncPaymentInstrumentDataRequest> {
                override fun serialize(t: AsyncPaymentInstrumentDataRequest): JSONObject {
                    return JSONObject().apply {
                        putOpt(TYPE_FIELD, t.type.name)
                        put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                        put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                        put(
                            SESSION_INFO_FIELD,
                            JSONSerializationUtils
                                .getJsonObjectSerializer<BaseSessionInfoDataRequest>()
                                .serialize(t.sessionInfo)
                        )
                    }
                }
            }
    }
}
