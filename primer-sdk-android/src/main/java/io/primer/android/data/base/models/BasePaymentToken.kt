package io.primer.android.data.base.models

import io.primer.android.model.dto.PaymentInstrumentData
import io.primer.android.threeds.data.models.ResponseCode
import kotlinx.serialization.Serializable

internal abstract class BasePaymentToken {
    abstract val token: String
    abstract val paymentInstrumentType: String
    abstract val paymentInstrumentData: PaymentInstrumentData?
    abstract val vaultData: VaultData?
    abstract val threeDSecureAuthentication: AuthenticationDetails?

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

    /**
     * Payment method name used for surcharge, i.e. PAYPAL instead of PAYPAL_BILLING_AGREEMENT.
     * Defaults to [paymentInstrumentType] in most cases.
     * */
    val surchargeType: String
        get() {
            if (paymentInstrumentType == "PAYPAL_BILLING_AGREEMENT") {
                return "PAYPAL"
            }

            return paymentInstrumentType
        }
}
