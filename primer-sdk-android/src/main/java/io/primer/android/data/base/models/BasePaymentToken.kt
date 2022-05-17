package io.primer.android.data.base.models

import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.payment.KLARNA_CUSTOMER_TOKEN_TYPE
import io.primer.android.payment.PAYPAL_BILLING_AGREEMENT_TYPE
import io.primer.android.threeds.data.models.ResponseCode
import kotlinx.serialization.Serializable

internal abstract class BasePaymentToken {
    abstract val token: String
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

    /**
     * Payment method name used for surcharge, i.e. PAYPAL instead of PAYPAL_BILLING_AGREEMENT.
     * Defaults to [paymentInstrumentType] in most cases.
     * */
    val surchargeType: String
        get() {
            if (paymentInstrumentType == PAYPAL_BILLING_AGREEMENT_TYPE) {
                return PaymentMethodType.PAYPAL.name
            } else if (paymentInstrumentType == KLARNA_CUSTOMER_TOKEN_TYPE) {
                return PaymentMethodType.KLARNA.name
            }

            return paymentInstrumentType
        }
}
