package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalBillingAgreement
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import org.json.JSONObject

internal data class PaypalCreateBillingAgreementDataResponse(
    val tokenId: String,
    val approvalUrl: String
) : JSONDeserializable {

    companion object {

        private const val TOKEN_ID_FIELD = "tokenId"
        private const val APPROVAL_URL_FIELD = "approvalUrl"

        @JvmField
        val deserializer = object : JSONDeserializer<PaypalCreateBillingAgreementDataResponse> {

            override fun deserialize(t: JSONObject): PaypalCreateBillingAgreementDataResponse {
                return PaypalCreateBillingAgreementDataResponse(
                    t.getString(TOKEN_ID_FIELD),
                    t.getString(APPROVAL_URL_FIELD)
                )
            }
        }
    }
}

internal fun PaypalCreateBillingAgreementDataResponse.toBillingAgreement(
    paymentMethodConfigId: String,
    successUrl: String,
    cancelUrl: String
) = PaypalBillingAgreement(
    paymentMethodConfigId,
    approvalUrl,
    successUrl,
    cancelUrl
)
