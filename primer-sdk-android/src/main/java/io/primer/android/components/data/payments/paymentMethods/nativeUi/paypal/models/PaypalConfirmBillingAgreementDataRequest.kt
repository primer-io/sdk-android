package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class PaypalConfirmBillingAgreementDataRequest(
    val paymentMethodConfigId: String,
    val tokenId: String
) : JSONObjectSerializable {

    companion object {

        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val TOKEN_ID_FIELD = "tokenId"

        @JvmField
        val serializer = object : JSONObjectSerializer<PaypalConfirmBillingAgreementDataRequest> {
            override fun serialize(t: PaypalConfirmBillingAgreementDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(TOKEN_ID_FIELD, t.tokenId)
                }
            }
        }
    }
}
