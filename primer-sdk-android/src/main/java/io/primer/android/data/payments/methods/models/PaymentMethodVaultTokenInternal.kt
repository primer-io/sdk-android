package io.primer.android.data.payments.methods.models

import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentMethodVaultTokenInternal(
    private val id: String,
    override val paymentMethodType: String? = null,
    override val paymentInstrumentType: String,
    override val paymentInstrumentData: PaymentInstrumentData? = null,
    override val vaultData: VaultData? = null,
    override val threeDSecureAuthentication: AuthenticationDetails? = null,
    override val isVaulted: Boolean,
) : BasePaymentToken() {

    override val token: String = id
}

internal fun PrimerPaymentMethodTokenData.toPaymentMethodVaultToken():
    PaymentMethodVaultTokenInternal {
    val paymentInstrumentData =
        if (paymentInstrumentData == null) null
        else PaymentInstrumentData(
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
    val vaultData =
        if (vaultData == null) null
        else BasePaymentToken.VaultData(customerId = vaultData.customerId)

    val threeDSecureAuthentication = threeDSecureAuthentication?.let {
        BasePaymentToken.AuthenticationDetails(
            it.responseCode,
            it.reasonCode,
            it.reasonText,
            it.protocolVersion,
            it.challengeIssued
        )
    }

    return PaymentMethodVaultTokenInternal(
        id = token,
        paymentMethodType = paymentInstrumentType,
        paymentInstrumentType = paymentInstrumentType,
        paymentInstrumentData = paymentInstrumentData,
        vaultData = vaultData,
        threeDSecureAuthentication = threeDSecureAuthentication,
        isVaulted = isVaulted
    )
}
