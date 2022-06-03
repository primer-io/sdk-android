package io.primer.android.data.tokenization.models

import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.threeds.data.models.ResponseCode
import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentMethodTokenInternal(
    override val token: String,
    override val paymentInstrumentType: String,
    override val paymentInstrumentData: PaymentInstrumentData? = null,
    override val vaultData: VaultData? = null,
    override val threeDSecureAuthentication: AuthenticationDetails? = null,
    override val isVaulted: Boolean,
    val analyticsId: String,
    val tokenType: TokenType,
) : BasePaymentToken() {

    fun setClientThreeDsError(errorMessage: String) =
        this.copy(
            threeDSecureAuthentication = AuthenticationDetails(
                ResponseCode.SKIPPED,
                "CLIENT_ERROR",
                errorMessage,
                "",
                false
            )
        )
}

internal fun PaymentMethodTokenInternal.toPaymentMethodToken() = PrimerPaymentMethodTokenData(
    token = token,
    analyticsId = analyticsId,
    tokenType = tokenType,
    paymentInstrumentType = paymentInstrumentType,
    paymentInstrumentData = paymentInstrumentData?.let { paymentInstrumentData ->
        PaymentInstrumentData(
            paymentInstrumentData.network,
            paymentInstrumentData.cardholderName,
            paymentInstrumentData.first6Digits,
            paymentInstrumentData.last4Digits,
            paymentInstrumentData.expirationMonth,
            paymentInstrumentData.expirationYear,
            paymentInstrumentData.gocardlessMandateId,
            paymentInstrumentData.externalPayerInfo,
            paymentInstrumentData.klarnaCustomerToken,
            paymentInstrumentData.sessionData,
            paymentInstrumentData.mx,
            paymentInstrumentData.mnc,
            paymentInstrumentData.mcc,
            paymentInstrumentData.hashedIdentifier,
            paymentInstrumentData.currencyCode,
            paymentInstrumentData.productId,
            paymentInstrumentData.paymentMethodType
        )
    },
    vaultData = vaultData?.let {
        PrimerPaymentMethodTokenData.VaultData(customerId = it.customerId)
    },
    threeDSecureAuthentication = threeDSecureAuthentication?.let {
        PrimerPaymentMethodTokenData.AuthenticationDetails(
            it.responseCode,
            it.reasonCode,
            it.reasonText,
            it.protocolVersion,
            it.challengeIssued
        )
    },
    isVaulted = isVaulted
)
