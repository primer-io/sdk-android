package io.primer.android.klarna.implementation.session.data.models

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import org.json.JSONObject

internal data class CreateVaultPaymentSessionDataRequest(
    val paymentMethodConfigId: String,
    val sessionType: KlarnaSessionType,
    val description: String?,
    val localeData: LocaleDataRequest,
) : JSONObjectSerializable {
    companion object {
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val SESSION_TYPE_FIELD = "sessionType"
        private const val DESCRIPTION_FIELD = "description"
        private const val LOCALE_DATA_FIELD = "localeData"

        val provider =
            object : WhitelistedHttpBodyKeysProvider {
                override val values: List<WhitelistedKey> =
                    whitelistedKeys {
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
        val serializer =
            JSONObjectSerializer<CreateVaultPaymentSessionDataRequest> { t ->
                JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(SESSION_TYPE_FIELD, t.sessionType.name)
                    putOpt(DESCRIPTION_FIELD, t.description)
                    put(
                        LOCALE_DATA_FIELD,
                        JSONSerializationUtils.getJsonObjectSerializer<LocaleDataRequest>()
                            .serialize(t.localeData),
                    )
                }
            }
    }
}
