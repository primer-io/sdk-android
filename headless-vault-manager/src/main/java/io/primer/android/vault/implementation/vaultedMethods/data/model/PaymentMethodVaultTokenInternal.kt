package io.primer.android.vault.implementation.vaultedMethods.data.model

import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.payments.core.tokenization.data.model.BasePaymentToken

internal data class PaymentMethodVaultTokenInternal(
    private val id: String,
    override val paymentMethodType: String,
    override val paymentInstrumentType: String,
    override val paymentInstrumentData: PaymentInstrumentData?,
    override val vaultData: VaultDataResponse?,
    override val threeDSecureAuthentication: AuthenticationDetailsDataResponse?,
    override val isVaulted: Boolean,
    val analyticsId: String,
) : BasePaymentToken() {
    override val token: String = id

    companion object {
        private const val ID_FIELD = "id"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val PAYMENT_INSTRUMENT_TYPE_FIELD = "paymentInstrumentType"
        private const val PAYMENT_INSTRUMENT_DATA_FIELD = "paymentInstrumentData"
        private const val VAULT_DATA_FIELD = "vaultData"
        private const val THREE_DS_AUTHENTICATION_FIELD = "threeDSecureAuthentication"
        private const val IS_VAULTED_FIELD = "isVaulted"
        private const val ANALYTICS_ID_FIELD = "analyticsId"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                PaymentMethodVaultTokenInternal(
                    t.getString(ID_FIELD),
                    t.getString(PAYMENT_METHOD_TYPE_FIELD),
                    t.getString(PAYMENT_INSTRUMENT_TYPE_FIELD),
                    t.optJSONObject(PAYMENT_INSTRUMENT_DATA_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<PaymentInstrumentData>()
                            .deserialize(it)
                    },
                    t.optJSONObject(VAULT_DATA_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<VaultDataResponse>()
                            .deserialize(it)
                    },
                    t.optJSONObject(THREE_DS_AUTHENTICATION_FIELD)?.let {
                        JSONSerializationUtils
                            .getJsonObjectDeserializer<AuthenticationDetailsDataResponse>()
                            .deserialize(it)
                    },
                    t.getBoolean(IS_VAULTED_FIELD),
                    t.optString(ANALYTICS_ID_FIELD),
                )
            }
    }
}

internal fun PaymentMethodVaultTokenInternal.toVaultedPaymentMethod() =
    PrimerVaultedPaymentMethod(
        id = token,
        analyticsId = analyticsId,
        paymentInstrumentType = paymentInstrumentType,
        paymentMethodType = paymentMethodType,
        paymentInstrumentData =
        requireNotNull(paymentInstrumentData).let { paymentInstrumentData ->
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
                sessionInfo = paymentInstrumentData.sessionInfo,
                binData = paymentInstrumentData.binData,
                bankName = paymentInstrumentData.bankName,
            )
        },
        threeDSecureAuthentication =
        threeDSecureAuthentication?.let {
            PrimerVaultedPaymentMethod.AuthenticationDetails(
                it.responseCode,
                it.reasonCode,
                it.reasonText,
                it.protocolVersion,
                it.challengeIssued,
            )
        },
    )
