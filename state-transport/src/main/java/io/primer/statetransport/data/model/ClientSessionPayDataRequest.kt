package io.primer.statetransport.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject

internal data class ClientSessionPayDataRequest(
    val paymentMethodConfigId: String?,
    val processorMerchantAccountId: String?,
    val paymentMethodType: String,
    val clientInfo: ClientSessionInfoDataRequest,
) : JSONObjectSerializable {

    companion object Companion {
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val PROCESSOR_MERCHANT_ACCOUNT_ID_FIELD = "processorMerchantAccountId"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val CLIENT_INFO_FIELD = "clientInfo"

        @JvmField
        val serializer = JSONObjectSerializer<ClientSessionPayDataRequest> { t ->
            JSONObject().apply {
                putOpt(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                putOpt(PROCESSOR_MERCHANT_ACCOUNT_ID_FIELD, t.processorMerchantAccountId)
                put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                put(
                    CLIENT_INFO_FIELD,
                    JSONSerializationUtils.getJsonObjectSerializer<ClientSessionInfoDataRequest>().serialize(
                        t.clientInfo,
                    ),
                )
            }
        }
    }
}
