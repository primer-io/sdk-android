package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.core.serialization.json.JSONSerializable
import io.primer.android.core.serialization.json.JSONSerializer
import org.json.JSONObject

internal data class PaypalCreateBillingAgreementDataRequest(
    val paymentMethodConfigId: String,
    val returnUrl: String,
    val cancelUrl: String
) : JSONSerializable {

    companion object {

        private const val PAYMENT_METHOD_CONFIG_ID_FIELD = "paymentMethodConfigId"
        private const val RETURN_URL_FIELD = "returnUrl"
        private const val CANCEL_URL_FIELD = "cancelUrl"

        @JvmField
        val serializer = object : JSONSerializer<PaypalCreateBillingAgreementDataRequest> {
            override fun serialize(t: PaypalCreateBillingAgreementDataRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_CONFIG_ID_FIELD, t.paymentMethodConfigId)
                    put(RETURN_URL_FIELD, t.returnUrl)
                    put(CANCEL_URL_FIELD, t.cancelUrl)
                }
            }
        }
    }
}
