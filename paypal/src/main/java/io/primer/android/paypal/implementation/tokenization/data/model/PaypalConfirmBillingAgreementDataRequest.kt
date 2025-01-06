package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import org.json.JSONObject

internal data class PaypalConfirmBillingAgreementDataRequest(
    val paymentMethodConfigId: String,
    val tokenId: String,
) : JSONObjectSerializable {
    companion object {
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val TOKEN_ID_FIELD = "tokenId"

        val provider =
            object : WhitelistedHttpBodyKeysProvider {
                override val values: List<WhitelistedKey> =
                    whitelistedKeys {
                        primitiveKey(PAYMENT_METHOD_CONFIG_ID_FIELD)
                    }
            }

        @JvmField
        val serializer =
            JSONObjectSerializer<PaypalConfirmBillingAgreementDataRequest> { t ->
                JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(TOKEN_ID_FIELD, t.tokenId)
                }
            }
    }
}
