package io.primer.android.data.base.models

import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.threeds.data.models.ResponseCode
import kotlinx.serialization.Serializable

internal abstract class BasePaymentToken {
    abstract val token: String
    abstract val paymentMethodType: String?
    abstract val paymentInstrumentType: String
    abstract val paymentInstrumentData: PaymentInstrumentData?
    abstract val vaultData: VaultData?
    abstract val threeDSecureAuthentication: AuthenticationDetails?
    abstract val isVaulted: Boolean

    @Serializable
    data class VaultData(
        val customerId: String,
    )

    @Serializable
    data class AuthenticationDetails(
        val responseCode: ResponseCode,
        val reasonCode: String? = null,
        val reasonText: String? = null,
        val protocolVersion: String? = null,
        val challengeIssued: Boolean? = null,
    )
}
