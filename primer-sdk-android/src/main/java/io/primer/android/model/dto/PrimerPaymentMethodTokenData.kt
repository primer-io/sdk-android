package io.primer.android.model.dto

import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.threeds.data.models.ResponseCode
import kotlinx.serialization.Serializable

/**
 * There's an issue with JsonObject & JSONObject here - need to replace
 * them all with gson or something.
 * For now we only expose JSONObject to the public
 */

internal object PaymentMethodTokenAdapter {

    fun internalToExternal(token: PaymentMethodTokenInternal): PrimerPaymentMethodTokenData {
        return PrimerPaymentMethodTokenData(
            token = token.token,
            analyticsId = token.analyticsId,
            tokenType = token.tokenType,
            paymentInstrumentType = token.paymentInstrumentType,
            paymentInstrumentData = token.paymentInstrumentData?.let { paymentInstrumentData ->
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
            vaultData = token.vaultData?.let {
                PrimerPaymentMethodTokenData.VaultData(customerId = it.customerId)
            },
            threeDSecureAuthentication = token.threeDSecureAuthentication?.let {
                PrimerPaymentMethodTokenData.AuthenticationDetails(
                    it.responseCode,
                    it.reasonCode,
                    it.reasonText,
                    it.protocolVersion,
                    it.challengeIssued
                )
            },
            isVaulted = token.isVaulted
        )
    }

    fun externalToInternal(token: PrimerPaymentMethodTokenData): PaymentMethodVaultTokenInternal {
        val paymentInstrumentData =
            if (token.paymentInstrumentData == null) null
            else PaymentInstrumentData(
                token.paymentInstrumentData.network,
                token.paymentInstrumentData.cardholderName,
                token.paymentInstrumentData.first6Digits,
                token.paymentInstrumentData.last4Digits,
                token.paymentInstrumentData.expirationMonth,
                token.paymentInstrumentData.expirationYear,
                token.paymentInstrumentData.gocardlessMandateId,
                token.paymentInstrumentData.externalPayerInfo,
                token.paymentInstrumentData.klarnaCustomerToken,
                token.paymentInstrumentData.sessionData,
                token.paymentInstrumentData.mx,
                token.paymentInstrumentData.mnc,
                token.paymentInstrumentData.mcc,
                token.paymentInstrumentData.hashedIdentifier,
                token.paymentInstrumentData.currencyCode,
                token.paymentInstrumentData.productId,
                token.paymentInstrumentData.paymentMethodType
            )
        val vaultData =
            if (token.vaultData == null) null
            else BasePaymentToken.VaultData(customerId = token.vaultData.customerId)

        val threeDSecureAuthentication = token.threeDSecureAuthentication?.let {
            BasePaymentToken.AuthenticationDetails(
                it.responseCode,
                it.reasonCode,
                it.reasonText,
                it.protocolVersion,
                it.challengeIssued
            )
        }

        return PaymentMethodVaultTokenInternal(
            id = token.token,
            paymentInstrumentType = token.paymentInstrumentType,
            paymentInstrumentData = paymentInstrumentData,
            vaultData = vaultData,
            threeDSecureAuthentication = threeDSecureAuthentication,
            isVaulted = token.isVaulted
        )
    }
}

data class PrimerPaymentMethodTokenData(
    val token: String,
    val analyticsId: String,
    val tokenType: TokenType,
    val paymentInstrumentType: String,
    val paymentInstrumentData: PaymentInstrumentData?,
    val vaultData: VaultData?,
    val threeDSecureAuthentication: AuthenticationDetails? = null,
    val isVaulted: Boolean
) {

    data class VaultData(
        val customerId: String,
    )

    data class AuthenticationDetails(
        val responseCode: ResponseCode,
        val reasonCode: String?,
        val reasonText: String?,
        val protocolVersion: String?,
        val challengeIssued: Boolean?,
    )
}

@Serializable
data class PaymentInstrumentData(
    val network: String? = null,
    val cardholderName: String? = null,
    val first6Digits: Int? = null,
    val last4Digits: Int? = null,
    val expirationMonth: Int? = null,
    val expirationYear: Int? = null,
    val gocardlessMandateId: String? = null,
    val externalPayerInfo: ExternalPayerInfo? = null,
    val klarnaCustomerToken: String? = null,
    val sessionData: SessionData? = null,
    // apaya
    val mx: String? = null,
    val mnc: Int? = null,
    val mcc: Int? = null,
    val hashedIdentifier: String? = null,
    val currencyCode: String? = null,
    val productId: String? = null,
    // async
    val paymentMethodType: String? = null,
    // bin
    val binData: BinData? = null,
)

@Serializable
data class ExternalPayerInfo(
    val email: String,
)

@Serializable
data class SessionData(
    val recurringDescription: String? = null,
    val billingAddress: BillingAddress? = null,
)

@Serializable
data class BillingAddress(
    val email: String,
)

@Serializable
data class BinData(
    val network: String? = null,
)

enum class TokenType {

    SINGLE_USE,
    MULTI_USE
}
