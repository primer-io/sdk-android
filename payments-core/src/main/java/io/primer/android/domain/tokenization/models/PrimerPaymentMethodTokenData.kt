// package structure is kept in order to maintain backward compatibility
package io.primer.android.domain.tokenization.models

import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.data.tokenization.models.TokenType
import io.primer.android.payments.core.tokenization.data.model.ResponseCode

data class PrimerPaymentMethodTokenData(
    val token: String,
    val analyticsId: String,
    val tokenType: TokenType,
    val paymentInstrumentType: String,
    val paymentMethodType: String?,
    val paymentInstrumentData: PaymentInstrumentData?,
    val vaultData: VaultData?,
    val threeDSecureAuthentication: AuthenticationDetails? = null,
    val isVaulted: Boolean
) {

    data class VaultData(
        val customerId: String
    )

    data class AuthenticationDetails(
        val responseCode: ResponseCode,
        val reasonCode: String?,
        val reasonText: String?,
        val protocolVersion: String?,
        val challengeIssued: Boolean?
    )
}
