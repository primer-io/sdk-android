package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalBillingAgreement

internal data class PaypalCreateBillingAgreementDataResponse(
    val tokenId: String,
    val approvalUrl: String,
) : JSONDeserializable {
    companion object {
        private const val TOKEN_ID_FIELD = "tokenId"
        private const val APPROVAL_URL_FIELD = "approvalUrl"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                PaypalCreateBillingAgreementDataResponse(
                    t.getString(TOKEN_ID_FIELD),
                    t.getString(APPROVAL_URL_FIELD),
                )
            }
    }
}

internal fun PaypalCreateBillingAgreementDataResponse.toBillingAgreement(
    paymentMethodConfigId: String,
    successUrl: String,
    cancelUrl: String,
) = PaypalBillingAgreement(
    paymentMethodConfigId,
    approvalUrl,
    successUrl,
    cancelUrl,
)
