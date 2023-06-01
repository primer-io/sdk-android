package io.primer.android.data.payments.methods.models

import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.data.tokenization.models.PaymentInstrumentData
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethodData
import org.json.JSONObject

internal data class PaymentMethodVaultTokenInternal(
    private val id: String,
    override val paymentMethodType: String,
    override val paymentInstrumentType: String,
    override val paymentInstrumentData: PaymentInstrumentData?,
    override val vaultData: VaultDataResponse?,
    override val threeDSecureAuthentication: AuthenticationDetailsDataResponse?,
    override val isVaulted: Boolean,
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

        @JvmField
        val deserializer = object : JSONDeserializer<PaymentMethodVaultTokenInternal> {

            override fun deserialize(t: JSONObject): PaymentMethodVaultTokenInternal {
                return PaymentMethodVaultTokenInternal(
                    t.getString(ID_FIELD),
                    t.getString(PAYMENT_METHOD_TYPE_FIELD),
                    t.getString(PAYMENT_INSTRUMENT_TYPE_FIELD),
                    t.optJSONObject(PAYMENT_INSTRUMENT_DATA_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<PaymentInstrumentData>()
                            .deserialize(it)
                    },
                    t.optJSONObject(VAULT_DATA_FIELD)?.let {
                        JSONSerializationUtils.getDeserializer<VaultDataResponse>()
                            .deserialize(it)
                    },
                    t.optJSONObject(THREE_DS_AUTHENTICATION_FIELD)?.let {
                        JSONSerializationUtils
                            .getDeserializer<AuthenticationDetailsDataResponse>()
                            .deserialize(it)
                    },
                    t.getBoolean(IS_VAULTED_FIELD),
                )
            }
        }
    }
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
        else BasePaymentToken.VaultDataResponse(customerId = vaultData.customerId)

    val threeDSecureAuthentication = threeDSecureAuthentication?.let {
        BasePaymentToken.AuthenticationDetailsDataResponse(
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

internal fun PaymentMethodVaultTokenInternal.toVaultedPaymentMethod() =
    PrimerVaultedPaymentMethodData(
        id = token,
        // FIXME
        analyticsId = "a",
        paymentInstrumentType = paymentInstrumentType,
        paymentMethodType = paymentMethodType,
        paymentInstrumentData = requireNotNull(paymentInstrumentData).let { paymentInstrumentData ->
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
        threeDSecureAuthentication = threeDSecureAuthentication?.let {
            PrimerVaultedPaymentMethodData.AuthenticationDetails(
                it.responseCode,
                it.reasonCode,
                it.reasonText,
                it.protocolVersion,
                it.challengeIssued
            )
        }
    )
