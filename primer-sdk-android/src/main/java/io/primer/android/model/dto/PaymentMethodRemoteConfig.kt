package io.primer.android.model.dto

import androidx.annotation.Keep
import io.primer.android.data.token.model.ClientTokenIntent
import kotlinx.serialization.Serializable

typealias PrimerPaymentMethodType = PaymentMethodType

@Keep
@Serializable
data class PaymentMethodRemoteConfig(
    val id: String? = null, // payment card has null only
    val type: PaymentMethodType = PaymentMethodType.UNKNOWN,
    val options: PaymentMethodRemoteConfigOptions? = null,
)

@Keep
@Serializable
enum class PaymentMethodType(internal val intent: ClientTokenIntent? = null) {
    PAYMENT_CARD(ClientTokenIntent.`3DS_AUTHENTICATION`),
    KLARNA,
    GOOGLE_PAY,
    PAYPAL,
    GOCARDLESS,
    APAYA,
    PAY_NL_IDEAL(ClientTokenIntent.PAY_NL_IDEAL_REDIRECTION),
    PAY_NL_PAYCONIQ(ClientTokenIntent.PAY_NL_PAYCONIQ_REDIRECTION),
    PAY_NL_GIROPAY(ClientTokenIntent.PAY_NL_GIROPAY_REDIRECTION),
    PAY_NL_P24(ClientTokenIntent.PAY_NL_P24_REDIRECTION),
    PAY_NL_EPS(ClientTokenIntent.PAY_NL_EPS_REDIRECTION),
    HOOLAH(ClientTokenIntent.HOOLAH_REDIRECTION),
    ADYEN_GIROPAY(ClientTokenIntent.ADYEN_GIROPAY_REDIRECTION),
    ADYEN_TWINT(ClientTokenIntent.ADYEN_TWINT_REDIRECTION),
    ADYEN_SOFORT(ClientTokenIntent.ADYEN_SOFORT_REDIRECTION),
    ADYEN_TRUSTLY(ClientTokenIntent.ADYEN_TRUSTLY_REDIRECTION),
    ADYEN_ALIPAY(ClientTokenIntent.ADYEN_ALIPAY_REDIRECTION),
    ADYEN_VIPPS(ClientTokenIntent.ADYEN_VIPPS_REDIRECTION),
    ADYEN_MOBILEPAY(ClientTokenIntent.ADYEN_MOBILEPAY_REDIRECTION),
    ADYEN_IDEAL(ClientTokenIntent.ADYEN_IDEAL_REDIRECTION),
    ADYEN_DOTPAY(ClientTokenIntent.ADYEN_DOTPAY_REDIRECTION),
    ADYEN_BLIK(ClientTokenIntent.ADYEN_BLIK_REDIRECTION),
    ADYEN_MBWAY(ClientTokenIntent.ADYEN_MBWAY_REDIRECTION),
    ADYEN_BANK_TRANSFER,
    MOLLIE_BANCONTACT(ClientTokenIntent.MOLLIE_BANCONTACT_REDIRECTION),
    MOLLIE_IDEAL(ClientTokenIntent.MOLLIE_IDEAL_REDIRECTION),
    MOLLIE_P24(ClientTokenIntent.MOLLIE_P24_REDIRECTION),
    MOLLIE_GIROPAY(ClientTokenIntent.MOLLIE_GIROPAY_REDIRECTION),
    MOLLIE_EPS(ClientTokenIntent.MOLLIE_EPS_REDIRECTION),
    BUCKAROO_GIROPAY(ClientTokenIntent.BUCKAROO_GIROPAY_REDIRECTION),
    BUCKAROO_SOFORT(ClientTokenIntent.BUCKAROO_SOFORT_REDIRECTION),
    BUCKAROO_IDEAL(ClientTokenIntent.BUCKAROO_IDEAL_REDIRECTION),
    BUCKAROO_EPS(ClientTokenIntent.BUCKAROO_EPS_REDIRECTION),
    BUCKAROO_BANCONTACT(ClientTokenIntent.BUCKAROO_BANCONTACT_REDIRECTION),
    ATOME(ClientTokenIntent.ATOME_REDIRECTION),
    XFERS_PAYNOW(ClientTokenIntent.XFERS_PAYNOW_REDIRECTION),
    UNKNOWN;

    companion object {
        fun safeValueOf(type: String?) = values().find { type == it.name }
            ?: UNKNOWN
    }
}

internal fun PaymentMethodType.toPrimerPaymentMethod(): PrimerPaymentMethod {
    return when (this) {
        PaymentMethodType.GOOGLE_PAY -> PrimerPaymentMethod.GOOGLE_PAY
        PaymentMethodType.APAYA -> PrimerPaymentMethod.APAYA
        PaymentMethodType.KLARNA -> PrimerPaymentMethod.KLARNA
        PaymentMethodType.PAYPAL -> PrimerPaymentMethod.PAYPAL
        PaymentMethodType.GOCARDLESS -> PrimerPaymentMethod.GOCARDLESS
        PaymentMethodType.ATOME -> PrimerPaymentMethod.ATOME
        PaymentMethodType.PAY_NL_IDEAL -> PrimerPaymentMethod.PAY_NL_IDEAL
        PaymentMethodType.PAY_NL_PAYCONIQ -> PrimerPaymentMethod.PAY_NL_PAYCONIQ
        PaymentMethodType.PAY_NL_GIROPAY -> PrimerPaymentMethod.PAY_NL_GIROPAY
        PaymentMethodType.HOOLAH -> PrimerPaymentMethod.HOOLAH
        PaymentMethodType.ADYEN_GIROPAY -> PrimerPaymentMethod.ADYEN_GIROPAY
        PaymentMethodType.ADYEN_TWINT -> PrimerPaymentMethod.ADYEN_TWINT
        PaymentMethodType.ADYEN_SOFORT -> PrimerPaymentMethod.ADYEN_SOFORT
        PaymentMethodType.ADYEN_TRUSTLY -> PrimerPaymentMethod.ADYEN_TRUSTLY
        PaymentMethodType.ADYEN_ALIPAY -> PrimerPaymentMethod.ADYEN_ALIPAY
        PaymentMethodType.ADYEN_VIPPS -> PrimerPaymentMethod.ADYEN_VIPPS
        PaymentMethodType.ADYEN_MOBILEPAY -> PrimerPaymentMethod.ADYEN_MOBILEPAY
        PaymentMethodType.MOLLIE_BANCONTACT -> PrimerPaymentMethod.MOLLIE_BANCONTACT
        PaymentMethodType.MOLLIE_IDEAL -> PrimerPaymentMethod.MOLLIE_IDEAL
        PaymentMethodType.BUCKAROO_GIROPAY -> PrimerPaymentMethod.BUCKAROO_GIROPAY
        PaymentMethodType.BUCKAROO_SOFORT -> PrimerPaymentMethod.BUCKAROO_SOFORT
        PaymentMethodType.BUCKAROO_IDEAL -> PrimerPaymentMethod.BUCKAROO_IDEAL
        PaymentMethodType.BUCKAROO_EPS -> PrimerPaymentMethod.BUCKAROO_EPS
        PaymentMethodType.BUCKAROO_BANCONTACT -> PrimerPaymentMethod.BUCKAROO_BANCONTACT
        PaymentMethodType.PAYMENT_CARD -> PrimerPaymentMethod.CARD
        else -> throw IllegalStateException("Unknown mapping for $this.")
    }
}

@Keep
@Serializable
data class PaymentMethodRemoteConfigOptions(
    val merchantId: String? = null,
    val merchantAccountId: String? = null,
    val threeDSecureEnabled: Boolean? = null,
)
