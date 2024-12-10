package io.primer.android.payments.core.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.data.tokenization.models.TokenType
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData

data class PaymentMethodTokenInternal(
    override val token: String,
    override val paymentInstrumentType: String,
    override val paymentMethodType: String? = paymentInstrumentType,
    override val paymentInstrumentData: PaymentInstrumentData?,
    override val vaultData: VaultDataResponse?,
    override val threeDSecureAuthentication: AuthenticationDetailsDataResponse?,
    override val isVaulted: Boolean,
    val analyticsId: String,
    val tokenType: TokenType
) : BasePaymentToken() {

    companion object {
        private const val TOKEN_FIELD = "token"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val PAYMENT_INSTRUMENT_TYPE_FIELD = "paymentInstrumentType"
        private const val PAYMENT_INSTRUMENT_DATA_FIELD = "paymentInstrumentData"
        private const val VAULT_DATA_FIELD = "vaultData"
        private const val THREE_DS_AUTHENTICATION_FIELD = "threeDSecureAuthentication"
        private const val IS_VAULTED_FIELD = "isVaulted"
        private const val ANALYTICS_ID_FIELD = "analyticsId"
        private const val TOKEN_TYPE_FIELD = "tokenType"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                val paymentInstrumentDataJson = t.optJSONObject(PAYMENT_INSTRUMENT_DATA_FIELD)
                val paymentInstrumentData = paymentInstrumentDataJson?.let {
                    JSONSerializationUtils.getJsonObjectDeserializer<PaymentInstrumentData>()
                        .deserialize(it)
                }
                val paymentMethodType = paymentInstrumentDataJson
                    ?.optNullableString(PAYMENT_METHOD_TYPE_FIELD)
                PaymentMethodTokenInternal(
                    token = t.getString(TOKEN_FIELD),
                    paymentInstrumentType = t.getString(PAYMENT_INSTRUMENT_TYPE_FIELD),
                    paymentMethodType = paymentMethodType ?: t.getString(PAYMENT_INSTRUMENT_TYPE_FIELD),
                    paymentInstrumentData = paymentInstrumentData,
                    vaultData = t.optJSONObject(VAULT_DATA_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<VaultDataResponse>()
                            .deserialize(it)
                    },
                    threeDSecureAuthentication = t.optJSONObject(THREE_DS_AUTHENTICATION_FIELD)?.let {
                        JSONSerializationUtils
                            .getJsonObjectDeserializer<AuthenticationDetailsDataResponse>()
                            .deserialize(it)
                    },
                    isVaulted = t.optBoolean(IS_VAULTED_FIELD),
                    analyticsId = t.getString(ANALYTICS_ID_FIELD),
                    tokenType = TokenType.valueOf(t.getString(TOKEN_TYPE_FIELD))
                )
            }
    }
}

fun PaymentMethodTokenInternal.toPaymentMethodToken() = PrimerPaymentMethodTokenData(
    token = token,
    analyticsId = analyticsId,
    tokenType = tokenType,
    paymentInstrumentType = paymentInstrumentType,
    paymentMethodType = paymentMethodType,
    paymentInstrumentData = paymentInstrumentData?.let { paymentInstrumentData ->
        PaymentInstrumentData(
            network = paymentInstrumentData.network,
            cardholderName = paymentInstrumentData.cardholderName,
            first6Digits = paymentInstrumentData.first6Digits,
            last4Digits = paymentInstrumentData.last4Digits,
            accountNumberLast4Digits = paymentInstrumentData.accountNumberLast4Digits,
            expirationMonth = paymentInstrumentData.expirationMonth,
            expirationYear = paymentInstrumentData.expirationYear,
            externalPayerInfo = paymentInstrumentData.externalPayerInfo,
            klarnaCustomerToken = paymentInstrumentData.klarnaCustomerToken,
            sessionData = paymentInstrumentData.sessionData,
            paymentMethodType = paymentInstrumentData.paymentMethodType,
            binData = null,
            bankName = paymentInstrumentData.bankName
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
