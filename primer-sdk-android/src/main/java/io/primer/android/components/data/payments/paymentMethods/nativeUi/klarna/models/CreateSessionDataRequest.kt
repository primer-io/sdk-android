package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import org.json.JSONObject

internal data class CreateSessionDataRequest(
    val paymentMethodConfigId: String,
    val sessionType: String,
    val description: String?,
    val localeData: LocaleDataRequest
) : JSONObjectSerializable {
    companion object {

        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val SESSION_TYPE_FIELD = "sessionType"
        private const val DESCRIPTION_FIELD = "description"
        private const val LOCALE_DATA_FIELD = "localeData"

        val provider = object : WhitelistedHttpBodyKeysProvider {
            override val values: List<WhitelistedKey> = whitelistedKeys {
                primitiveKey(PAYMENT_METHOD_CONFIG_ID_FIELD)
                primitiveKey(SESSION_TYPE_FIELD)
                nonPrimitiveKey(LOCALE_DATA_FIELD) {
                    primitiveKey(LocaleDataRequest.COUNTRY_CODE_FIELD)
                    primitiveKey(LocaleDataRequest.CURRENCY_CODE_FIELD)
                    primitiveKey(LocaleDataRequest.LOCALE_CODE_FIELD)
                }
            }
        }

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
