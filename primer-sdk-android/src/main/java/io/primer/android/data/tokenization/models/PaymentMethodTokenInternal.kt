package io.primer.android.data.tokenization.models

import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import org.json.JSONObject

internal data class PaymentMethodTokenInternal(
    override val token: String,
    override val paymentInstrumentType: String,
    override val paymentMethodType: String? = paymentInstrumentType,
    override val paymentInstrumentData: PaymentInstrumentData?,
    override val vaultData: VaultDataResponse?,
    override val threeDSecureAuthentication: AuthenticationDetailsDataResponse?,
    override val isVaulted: Boolean,
    val analyticsId: String,
    val tokenType: TokenType,
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
        val deserializer = object : JSONDeserializer<PaymentMethodTokenInternal> {

            override fun deserialize(t: JSONObject): PaymentMethodTokenInternal {
                val paymentInstrumentDataJson = t.optJSONObject(PAYMENT_INSTRUMENT_DATA_FIELD)
                val paymentInstrumentData = paymentInstrumentDataJson?.let {
                    JSONSerializationUtils.getDeserializer<PaymentInstrumentData>()
                        .deserialize(it)
                }
                val paymentMethodType = paymentInstrumentDataJson
                    ?.optNullableString(PAYMENT_METHOD_TYPE_FIELD)
                return PaymentMethodTokenInternal(
                    t.getString(TOKEN_FIELD),
                    t.getString(PAYMENT_INSTRUMENT_TYPE_FIELD),
                    paymentMethodType ?: t.getString(PAYMENT_INSTRUMENT_TYPE_FIELD),
                    paymentInstrumentData,
                    t.optJSONObject(VAULT_DATA_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<VaultDataResponse>()
                            .deserialize(it)
                    },
                    t.optJSONObject(THREE_DS_AUTHENTICATION_FIELD)?.let {
                        JSONSerializationUtils
                            .getDeserializer<AuthenticationDetailsDataResponse>()
                            .deserialize(it)
                    },
                    t.optBoolean(IS_VAULTED_FIELD),
                    t.getString(ANALYTICS_ID_FIELD),
                    TokenType.valueOf(t.getString(TOKEN_TYPE_FIELD))
                )
            }
        }
    }
}

internal fun PaymentMethodTokenInternal.toPaymentMethodToken() = PrimerPaymentMethodTokenData(
    token = token,
    analyticsId = analyticsId,
    tokenType = tokenType,
    paymentInstrumentType = paymentInstrumentType,
    paymentMethodType = paymentMethodType,
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
