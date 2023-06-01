package io.primer.android.domain.tokenization.models

import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.threeds.data.models.common.ResponseCode

data class PrimerVaultedPaymentMethodData(
    val id: String,
    val analyticsId: String,
    val paymentInstrumentType: String,
    val paymentMethodType: String,
    val paymentInstrumentData: PaymentInstrumentData,
    val threeDSecureAuthentication: AuthenticationDetails? = null,
) {

    data class AuthenticationDetails(
        val responseCode: ResponseCode,
        val reasonCode: String?,
        val reasonText: String?,
        val protocolVersion: String?,
        val challengeIssued: Boolean?,
    )
}
