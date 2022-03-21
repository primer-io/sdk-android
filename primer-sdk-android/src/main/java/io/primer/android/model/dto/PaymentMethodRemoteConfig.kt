package io.primer.android.model.dto

import androidx.annotation.Keep
import io.primer.android.components.ui.assets.Brand
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
enum class PaymentMethodType(
    internal val intents: Array<ClientTokenIntent>? = null,
    internal val brand: Brand
) {
    PAYMENT_CARD(
        arrayOf(
            ClientTokenIntent.`3DS_AUTHENTICATION`,
            ClientTokenIntent.PROCESSOR_3DS,
        ),
        Brand.PAYMENT_CARD
    ),
    KLARNA(brand = Brand.KLARNA),
    GOOGLE_PAY(brand = Brand.GOOGLE_PAY),
    PAYPAL(brand = Brand.PAYPAL),
    GOCARDLESS(brand = Brand.GOCARDLESS),
    APAYA(brand = Brand.APAYA),
    PAY_NL_IDEAL(ClientTokenIntent.PAY_NL_IDEAL_REDIRECTION, Brand.IDEAL),
    PAY_NL_PAYCONIQ(ClientTokenIntent.PAY_NL_PAYCONIQ_REDIRECTION, Brand.PAYQONIC),
    PAY_NL_GIROPAY(ClientTokenIntent.PAY_NL_GIROPAY_REDIRECTION, Brand.GIROPAY),
    PAY_NL_P24(ClientTokenIntent.PAY_NL_P24_REDIRECTION, Brand.P24),
    PAY_NL_EPS(ClientTokenIntent.PAY_NL_EPS_REDIRECTION, Brand.EPS),
    HOOLAH(ClientTokenIntent.HOOLAH_REDIRECTION, Brand.HOOLAH),
    ADYEN_GIROPAY(ClientTokenIntent.ADYEN_GIROPAY_REDIRECTION, Brand.GIROPAY),
    ADYEN_TWINT(ClientTokenIntent.ADYEN_TWINT_REDIRECTION, Brand.TWINT),
    ADYEN_SOFORT(ClientTokenIntent.ADYEN_SOFORT_REDIRECTION, Brand.SOFORT),
    ADYEN_TRUSTLY(ClientTokenIntent.ADYEN_TRUSTLY_REDIRECTION, Brand.TRUSTLY),
    ADYEN_ALIPAY(ClientTokenIntent.ADYEN_ALIPAY_REDIRECTION, Brand.ALIPAY),
    ADYEN_VIPPS(ClientTokenIntent.ADYEN_VIPPS_REDIRECTION, Brand.VIPPS),
    ADYEN_MOBILEPAY(ClientTokenIntent.ADYEN_MOBILEPAY_REDIRECTION, Brand.MOBILEPAY),
    ADYEN_IDEAL(ClientTokenIntent.ADYEN_IDEAL_REDIRECTION, Brand.IDEAL),
    ADYEN_DOTPAY(ClientTokenIntent.ADYEN_DOTPAY_REDIRECTION, Brand.DOTPAY),
    ADYEN_BLIK(ClientTokenIntent.ADYEN_BLIK_REDIRECTION, Brand.BLIK),
    ADYEN_MBWAY(ClientTokenIntent.ADYEN_MBWAY_REDIRECTION, Brand.MBWAY),
    ADYEN_INTERAC(ClientTokenIntent.ADYEN_INTERAC_REDIRECTION, Brand.INTERAC),
    ADYEN_PAYTRAIL(ClientTokenIntent.ADYEN_PAYTRAIL_REDIRECTION, Brand.PAYTRAIL),
    ADYEN_PAYSHOP(ClientTokenIntent.ADYEN_PAYSHOP_REDIRECTION, Brand.PAYSHOP),
    ADYEN_BANK_TRANSFER(brand = Brand.BANK_TRANSFER),
    MOLLIE_BANCONTACT(ClientTokenIntent.MOLLIE_BANCONTACT_REDIRECTION, Brand.BANCONTACT),
    MOLLIE_IDEAL(ClientTokenIntent.MOLLIE_IDEAL_REDIRECTION, Brand.IDEAL),
    MOLLIE_P24(ClientTokenIntent.MOLLIE_P24_REDIRECTION, Brand.P24),
    MOLLIE_GIROPAY(ClientTokenIntent.MOLLIE_GIROPAY_REDIRECTION, Brand.GIROPAY),
    MOLLIE_EPS(ClientTokenIntent.MOLLIE_EPS_REDIRECTION, Brand.EPS),
    BUCKAROO_GIROPAY(ClientTokenIntent.BUCKAROO_GIROPAY_REDIRECTION, Brand.GIROPAY),
    BUCKAROO_SOFORT(ClientTokenIntent.BUCKAROO_SOFORT_REDIRECTION, Brand.SOFORT),
    BUCKAROO_IDEAL(ClientTokenIntent.BUCKAROO_IDEAL_REDIRECTION, Brand.IDEAL),
    BUCKAROO_EPS(ClientTokenIntent.BUCKAROO_EPS_REDIRECTION, Brand.EPS),
    BUCKAROO_BANCONTACT(ClientTokenIntent.BUCKAROO_BANCONTACT_REDIRECTION, Brand.BANCONTACT),
    ATOME(ClientTokenIntent.ATOME_REDIRECTION, Brand.ATOME),
    XFERS_PAYNOW(ClientTokenIntent.XFERS_PAYNOW_REDIRECTION, Brand.PAYNOW),
    UNKNOWN(brand = Brand.UNKNOWN);

    companion object {
        fun safeValueOf(type: String?) = values().find { type == it.name }
            ?: UNKNOWN
    }

    constructor(intent: ClientTokenIntent, brand: Brand) : this(arrayOf(intent), brand)
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
        PaymentMethodType.ADYEN_INTERAC -> PrimerPaymentMethod.ADYEN_INTERAC
        PaymentMethodType.ADYEN_PAYTRAIL -> PrimerPaymentMethod.ADYEN_PAYTRAIL
        PaymentMethodType.ADYEN_PAYSHOP -> PrimerPaymentMethod.ADYEN_PAYSHOP
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
