package io.primer.android.data.tokenization.models

import android.os.Build
import android.util.Base64
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
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
import io.primer.android.data.tokenization.models.paymentInstruments.klarna.KlarnaCheckoutPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.klarna.KlarnaVaultPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.nolpay.NolPaySessionInfoDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.paypal.ExternalPayerInfoRequest
import io.primer.android.data.tokenization.models.paymentInstruments.paypal.PaypalCheckoutPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.paypal.PaypalVaultPaymentInstrumentDataRequest
import io.primer.android.data.tokenization.models.paymentInstruments.stripe.ach.StripeAchSessionInfoDataRequest
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
import io.primer.android.domain.tokenization.models.paymentInstruments.klarna.KlarnaCheckoutPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.klarna.KlarnaVaultPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.nolpay.NolPayPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.paypal.PaypalCheckoutPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.paypal.PaypalVaultPaymentInstrumentParams
import io.primer.android.domain.tokenization.models.paymentInstruments.stripe.ach.StripeAchPaymentInstrumentParams
import io.primer.android.payment.async.adyen.vipps.helpers.PlatformResolver
import org.json.JSONObject

internal abstract class TokenizationRequestV2 : JSONObjectSerializable {

    abstract val paymentInstrument: PaymentInstrumentDataRequest

    companion object {
        @JvmField
        val serializer = JSONObjectSerializer<TokenizationRequestV2> { t ->
            when (t) {
                is TokenizationCheckoutRequestV2 ->
                    TokenizationCheckoutRequestV2.serializer.serialize(t)

                is TokenizationVaultRequestV2 ->
                    TokenizationVaultRequestV2.serializer.serialize(t)

                else -> throw IllegalStateException("Unsupported instance of $t")
            }
        }
    }
}

internal fun TokenizationParamsV2.toTokenizationRequest(): TokenizationRequestV2 {
    return when (paymentMethodIntent) {
        PrimerSessionIntent.CHECKOUT, null -> TokenizationCheckoutRequestV2(
            paymentInstrumentParams.toPaymentInstrumentData()
        )

        PrimerSessionIntent.VAULT -> TokenizationVaultRequestV2(
            paymentInstrumentParams.toPaymentInstrumentData(),
            TokenType.MULTI_USE.name,
            paymentMethodIntent.name
        )
    }
}

@Suppress("LongMethod", "ComplexMethod")
internal fun BasePaymentInstrumentParams.toPaymentInstrumentData(): PaymentInstrumentDataRequest {
    return when (this) {
        is CardPaymentInstrumentParams -> CardPaymentInstrumentDataRequest(
            number,
            expirationMonth,
            expirationYear,
            cvv,
            cardholderName,
            preferredNetwork
        )

        is KlarnaCheckoutPaymentInstrumentParams -> KlarnaCheckoutPaymentInstrumentDataRequest(
            klarnaAuthorizationToken = klarnaAuthorizationToken,
            sessionData = sessionData
        )

        is KlarnaVaultPaymentInstrumentParams -> KlarnaVaultPaymentInstrumentDataRequest(
            klarnaCustomerToken = klarnaCustomerToken,
            sessionData = sessionData
        )

        is PaypalCheckoutPaymentInstrumentParams -> PaypalCheckoutPaymentInstrumentDataRequest(
            paypalOrderId,
            ExternalPayerInfoRequest(
                email = externalPayerInfoEmail,
                externalPayerId = externalPayerId,
                firstName = externalPayerFirstName,
                lastName = externalPayerLastName

            )
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

        is WebRedirectPaymentInstrumentParams -> {
            val platform = PlatformResolver.getPlatform(paymentMethodType)

            AsyncPaymentInstrumentDataRequest(
                paymentMethodType,
                paymentMethodConfigId,
                WebRedirectSessionInfoDataRequest(locale, redirectionUrl, platform),
                type
            )
        }

        is AdyenBlikPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            BlikSessionInfoDataRequest(locale, redirectionUrl, blikCode),
            type
        )

        is PhonePaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            PhoneNumberSessionInfoDataRequest(locale, redirectionUrl, phoneNumber),
            type
        )

        is BankIssuerPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            BankIssuerSessionInfoDataRequest(locale, redirectionUrl, bankIssuer),
            type
        )

        is PrimerDummyPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            PrimerDummySessionInfoDataRequest(locale, redirectionUrl, flowDecisionType),
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
                AdyenBancontactSessionInfoDataRequest(locale, redirectionUrl, userAgent),
                type
            )

        is XenditRetailOutletPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            RetailOutletsSessionInfoDataRequest(locale, redirectionUrl, retailOutlet),
            type
        )

        is NolPayPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType,
            paymentMethodConfigId,
            NolPaySessionInfoDataRequest(
                mobileCountryCode,
                mobileNumber,
                nolPayCardNumber,
                Build.MANUFACTURER,
                Build.MODEL,
                locale
            ),
            type
        )

        is StripeAchPaymentInstrumentParams -> AsyncPaymentInstrumentDataRequest(
            paymentMethodType = paymentMethodType,
            paymentMethodConfigId = paymentMethodConfigId,
            sessionInfo = StripeAchSessionInfoDataRequest(locale = locale),
            type = type,
            authenticationProvider = authenticationProvider
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
        val serializer = JSONObjectSerializer<TokenizationVaultRequestV2> { t ->
            JSONObject().apply {
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

internal data class TokenizationCheckoutRequestV2(
    override val paymentInstrument: PaymentInstrumentDataRequest
) : TokenizationRequestV2() {
    companion object {
        private const val PAYMENT_INSTRUMENT_FIELD = "paymentInstrument"

        @JvmField
        val serializer = JSONObjectSerializer<TokenizationCheckoutRequestV2> { t ->
            JSONObject().apply {
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
