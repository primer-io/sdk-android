package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
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

        val provider = object : WhitelistedHttpBodyKeysProvider {
            override val values: List<WhitelistedKey> = whitelistedKeys {
                primitiveKey(PAYMENT_METHOD_CONFIG_ID_FIELD)
                primitiveKey(SESSION_ID_FIELD)
                nonPrimitiveKey(LOCALE_DATA_FIELD) {
                    primitiveKey(LocaleDataRequest.COUNTRY_CODE_FIELD)
                    primitiveKey(LocaleDataRequest.CURRENCY_CODE_FIELD)
                    primitiveKey(LocaleDataRequest.LOCALE_CODE_FIELD)
                }
            }
        }

        @JvmField
        val serializer = JSONObjectSerializer<CreateCustomerTokenDataRequest> { t ->
            JSONObject().apply {
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
