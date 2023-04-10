package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class CreateSessionDataRequest(
    val paymentMethodConfigId: String,
    val sessionType: String,
    val description: String?,
    val localeData: LocaleDataRequest,
) : JSONObjectSerializable {
    companion object {

        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val SESSION_TYPE_FIELD = "sessionType"
        private const val DESCRIPTION_FIELD = "description"
        private const val LOCALE_DATA_FIELD = "localeData"

        @JvmField
        val serializer = object : JSONObjectSerializer<CreateSessionDataRequest> {
            override fun serialize(t: CreateSessionDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(SESSION_TYPE_FIELD, t.sessionType)
                    putOpt(DESCRIPTION_FIELD, t.description)
                    put(
                        LOCALE_DATA_FIELD,
                        JSONSerializationUtils.getJsonObjectSerializer<LocaleDataRequest>()
                            .serialize(t.localeData)
                    )
                }
            }
        }
    }
}
