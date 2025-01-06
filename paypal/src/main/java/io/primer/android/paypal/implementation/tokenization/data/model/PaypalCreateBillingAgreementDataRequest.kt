package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import org.json.JSONObject

internal data class PaypalCreateBillingAgreementDataRequest(
    val paymentMethodConfigId: String,
    val returnUrl: String,
    val cancelUrl: String,
) : JSONObjectSerializable {
    companion object {
        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val RETURN_URL_FIELD = "returnUrl"
        private const val CANCEL_URL_FIELD = "cancelUrl"

        val provider =
            object : WhitelistedHttpBodyKeysProvider {
                override val values: List<WhitelistedKey> =
                    whitelistedKeys {
                        primitiveKey(PAYMENT_METHOD_CONFIG_ID_FIELD)
                        primitiveKey(RETURN_URL_FIELD)
                        primitiveKey(CANCEL_URL_FIELD)
                    }
            }

        @JvmField
        val serializer =
            JSONObjectSerializer<PaypalCreateBillingAgreementDataRequest> { t ->
                JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(RETURN_URL_FIELD, t.returnUrl)
                    put(CANCEL_URL_FIELD, t.cancelUrl)
                }
            }
    }
}
