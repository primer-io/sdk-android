package io.primer.android.data.tokenization.models

import android.util.Base64
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.data.tokenization.models.paymentInstruments.async.AsyncPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.bancontactCard.AdyenBancontactCardPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.bancontactCard.AdyenBancontactSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.bankIssuer.BankIssuerSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.blik.BlikSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.dummy.PrimerDummySessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.phone.PhoneNumberSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.retailOutlets.RetailOutletsSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.async.webRedirect.WebRedirectSessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.card.CardPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.googlepay.GooglePayPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.klarna.KlarnaPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.paypal.ExternalPayerInfoRequest
import io.primer.android.data.tokenization.models.paymentInstruments.paypal.PaypalCheckoutPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.paypal.PaypalVaultPaymentInstrumentDataRequest
import io.primer.android.domain.tokenization.models.TokenizationParamsV2
import io.primer.android.domain.tokenization.models.paymentInstruments.BasePaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.async.bankIssuer.BankIssuerPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.async.blik.AdyenBlikPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.async.dummy.PrimerDummyPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.async.phone.PhonePaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.async.retailOutlet.XenditRetailOutletPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.async.webRedirect.WebRedirectPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.bancontactCard.AdyenBancontactCardPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.card.CardPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.googlepay.GooglePayPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.klarna.KlarnaPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.paypal.PaypalCheckoutPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.paypal.PaypalVaultPaymentInstrumentParams
import org.json.JSONObject

internal abstract class TokenizationRequestV2 : JSONObjectSerializable {

    abstract val paymentInstrument: PaymentInstrumentDataRequest

    companion object {
        @JvmField
        val serializer = object : JSONObjectSerializer<TokenizationRequestV2> {
            override fun serialize(t: TokenizationRequestV2): JSONObject {
                return when (t) {
                    is TokenizationCheckoutRequestV2 ->
                        TokenizationCheckoutRequestV2.serializer.serialize(t)
                    is TokenizationVaultRequestV2 ->
                        TokenizationVaultRequestV2.serializer.serialize(t)
                    else -> throw IllegalStateException("Unsupported instance of $t")
                }
            }
        }
    }
}

internal fun TokenizationParamsV2.toTokenizationRequest(): TokenizationRequestV2 {
    return when (paymentMethodIntent) {
        PrimerSessionIntent.CHECKOUT -> TokenizationCheckoutRequestV2(
            paymentInstrumentParams.toPaymentInstrumentData()
        )
        PrimerSessionIntent.VAULT -> TokenizationVaultRequestV2(
            paymentInstrumentParams.toPaymentInstrumentData(),
            TokenType.MULTI_USE.name,
            paymentMethodIntent.name
        )
    }
}

internal fun BasePaymentInstrumentParams.toPaymentInstrumentData(): PaymentInstrumentDataRequest {
    return when (this) {
        is CardPaymentInstrumentParams -> CardPaymentInstrumentDataRequest(
            number,
            expirationMonth,
            expirationYear,
            cvv,
            cardholderName
        )
        is KlarnaPaymentInstrumentParams -> KlarnaPaymentInstrumentDataRequest(
            klarnaCustomerToken,
            sessionData
        )
        is PaypalCheckoutPaymentInstrumentParams -> PaypalCheckoutPaymentInstrumentDataRequest(
            paypalOrderId,
            ExternalPayerInfoRequest(externalPayerInfoEmail)
        )
        is PaypalVaultPaymentInstrumentParams -> PaypalVaultPaymentInstrumentDataRequest(
            paypalBillingAgreementId,
            externalPayerInfo,
            shippingAddress
        )
        is GooglePayPaymentInstrumentParams
        -> {
            val token =
                JSONObject(paymentData.toJson()).getJSONObject("paymentMethodData")
                    .getJSONObject("tokenizationData")
                    .getString("token")
            GooglePayPaymentInstrumentDataRequest(
                merchantId,
                Base64.encodeToString(token.toByteArray(), Base64.NO_WRAP),
                flow
            )
        }
        is WebRedirectPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            WebRedirectSessionInfoDataRequest(locale, redirectionUrl),
            type
        )
        is AdyenBlikPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            BlikSessionInfoDataRequest(blikCode, locale, redirectionUrl),
            type
        )
        is PhonePaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            PhoneNumberSessionInfoDataRequest(phoneNumber, locale, redirectionUrl),
            type
        )
        is BankIssuerPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            BankIssuerSessionInfoDataRequest(bankIssuer, locale, redirectionUrl),
            type
        )
        is PrimerDummyPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            PrimerDummySessionInfoDataRequest(flowDecisionType, locale, redirectionUrl),
            type
        )
        is AdyenBancontactCardPaymentInstrumentParams ->
            AdyenBancontactCardPaymentInstrumentDataRequest(
                number,
                expirationMonth,
                expirationYear,
                cardholderName,
                paymentMethodType,
                paymentMethodConfigId,
                AdyenBancontactSessionInfoDataRequest(userAgent, locale, redirectionUrl),
                type
            )
        is XenditRetailOutletPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            RetailOutletsSessionInfoDataRequest(retailOutlet, locale, redirectionUrl),
            type
        )
        else -> throw IllegalArgumentException("Missing PaymentInstrumentParams mapping for $this")
    }
}

internal data class TokenizationVaultRequestV2(
    override val paymentInstrument: PaymentInstrumentDataRequest,
    private val tokenType: String,
    private val paymentFlow: String
) : TokenizationRequestV2() {

    companion object {
        private const val PAYMENT_INSTRUMENT_FIELD = "paymentInstrument"
        private const val TOKEN_TYPE_FIELD = "tokenType"
        private const val PAYMENT_FLOW_FIELD = "paymentFlow"

        @JvmField
        val serializer = object : JSONObjectSerializer<TokenizationVaultRequestV2> {
            override fun serialize(t: TokenizationVaultRequestV2): JSONObject {
                return JSONObject().apply {
                    put(
                        PAYMENT_INSTRUMENT_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<PaymentInstrumentDataRequest>()
                            .serialize(t.paymentInstrument)
                    )
                    put(TOKEN_TYPE_FIELD, t.tokenType)
                    put(PAYMENT_FLOW_FIELD, t.paymentFlow)
                }
            }
        }
    }
}

internal data class TokenizationCheckoutRequestV2(
    override val paymentInstrument: PaymentInstrumentDataRequest
) : TokenizationRequestV2() {
    companion object {
        private const val PAYMENT_INSTRUMENT_FIELD = "paymentInstrument"

        @JvmField
        val serializer = object : JSONObjectSerializer<TokenizationCheckoutRequestV2> {
            override fun serialize(t: TokenizationCheckoutRequestV2): JSONObject {
                return JSONObject().apply {
                    put(
                        PAYMENT_INSTRUMENT_FIELD,
                        JSONSerializationUtils
                            .getJsonObjectSerializer<PaymentInstrumentDataRequest>()
                            .serialize(t.paymentInstrument)
                    )
                }
            }
        }
    }
}
