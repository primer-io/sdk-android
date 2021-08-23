package io.primer.android.model.dto

import io.primer.android.threeds.data.models.ResponseCode
import kotlinx.serialization.Serializable

/**
 * There's an issue with JsonObject & JSONObject here - need to replace
 * them all with gson or something.
 * For now we only expose JSONObject to the public
 */

@Serializable
internal data class PaymentMethodTokenInternal(
    val token: String,
    val analyticsId: String,
    val tokenType: TokenType,
    val paymentInstrumentType: String,
    val paymentInstrumentData: PaymentInstrumentData? = null,
    val vaultData: VaultData? = null,
    val threeDSecureAuthentication: AuthenticationDetails? = null,
) {

    @Serializable
    data class VaultData(
        val customerId: String,
    )

    @Serializable
    data class AuthenticationDetails(
        val responseCode: ResponseCode,
        val reasonCode: String?,
        val reasonText: String?,
        val protocolVersion: String?,
        val challengeIssued: Boolean?,
    )

    internal fun setClientThreeDsError(errorMessage: String) =
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

internal object PaymentMethodTokenAdapter {

    fun internalToExternal(token: PaymentMethodTokenInternal): PaymentMethodToken {
        return PaymentMethodToken(
            token = token.token,
            analyticsId = token.analyticsId,
            tokenType = token.tokenType,
            paymentInstrumentType = token.paymentInstrumentType,
            paymentInstrumentData = if (token.paymentInstrumentData == null) null
            else PaymentInstrumentData(
                token.paymentInstrumentData.network,
                token.paymentInstrumentData.cardholderName,
                token.paymentInstrumentData.last4Digits,
                token.paymentInstrumentData.expirationMonth,
                token.paymentInstrumentData.expirationYear,
                token.paymentInstrumentData.gocardlessMandateId,
                token.paymentInstrumentData.externalPayerInfo,
                token.paymentInstrumentData.klarnaCustomerToken,
                token.paymentInstrumentData.sessionData
            ),
            vaultData = if (token.vaultData == null) null else PaymentMethodToken.VaultData(
                customerId = token.vaultData.customerId
            ),
            threeDSecureAuthentication = token.threeDSecureAuthentication?.let {
                PaymentMethodToken.AuthenticationDetails(
                    it.responseCode,
                    it.reasonCode,
                    it.reasonText,
                    it.protocolVersion,
                    it.challengeIssued
                )
            }
        )
    }

    fun externalToInternal(token: PaymentMethodToken): PaymentMethodTokenInternal {
        val paymentInstrumentData =
            if (token.paymentInstrumentData == null) null
            else PaymentInstrumentData(
                token.paymentInstrumentData.network,
                token.paymentInstrumentData.cardholderName,
                token.paymentInstrumentData.last4Digits,
                token.paymentInstrumentData.expirationMonth,
                token.paymentInstrumentData.expirationYear,
                token.paymentInstrumentData.gocardlessMandateId,
                token.paymentInstrumentData.externalPayerInfo,
                token.paymentInstrumentData.klarnaCustomerToken,
                token.paymentInstrumentData.sessionData
            )
        val vaultData =
            if (token.vaultData == null) null
            else PaymentMethodTokenInternal.VaultData(customerId = token.vaultData.customerId)

        val threeDSecureAuthentication = token.threeDSecureAuthentication?.let {
            PaymentMethodTokenInternal.AuthenticationDetails(
                it.responseCode,
                it.reasonCode,
                it.reasonText,
                it.protocolVersion,
                it.challengeIssued
            )
        }

        return PaymentMethodTokenInternal(
            token = token.token,
            analyticsId = token.analyticsId,
            tokenType = token.tokenType,
            paymentInstrumentType = token.paymentInstrumentType,
            paymentInstrumentData = paymentInstrumentData,
            vaultData = vaultData,
            threeDSecureAuthentication = threeDSecureAuthentication
        )
    }
}

data class PaymentMethodToken(
    val token: String,
    val analyticsId: String,
    val tokenType: TokenType,
    val paymentInstrumentType: String,
    val paymentInstrumentData: PaymentInstrumentData?,
    val vaultData: VaultData?,
    val threeDSecureAuthentication: AuthenticationDetails? = null,
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
    val last4Digits: Int? = null,
    val expirationMonth: Int? = null,
    val expirationYear: Int? = null,
    val gocardlessMandateId: String? = null,
    val externalPayerInfo: ExternalPayerInfo? = null,
    val klarnaCustomerToken: String? = null,
    val sessionData: SessionData? = null,
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
