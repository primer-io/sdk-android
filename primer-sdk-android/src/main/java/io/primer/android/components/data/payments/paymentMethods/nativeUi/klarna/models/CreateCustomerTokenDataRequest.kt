package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class CreateCustomerTokenDataRequest(
    private val paymentMethodConfigId: String,
    private val sessionId: String,
    private val authorizationToken: String,
    private val description: String?,
    private val localeData: LocaleDataRequest
) : JSONObjectSerializable {
    companion object {

        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val SESSION_ID_FIELD = "sessionId"
        private const val AUTHORIZATION_TOKEN_FIELD = "authorizationToken"
        private const val DESCRIPTION_FIELD = "description"
        private const val LOCALE_DATA_FIELD = "localeData"

        @JvmField
        val serializer = object : JSONObjectSerializer<CreateCustomerTokenDataRequest> {
            override fun serialize(t: CreateCustomerTokenDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(SESSION_ID_FIELD, t.sessionId)
                    put(AUTHORIZATION_TOKEN_FIELD, t.authorizationToken)
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
