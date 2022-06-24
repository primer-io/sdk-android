package io.primer.android.data.configuration.models

import androidx.annotation.RestrictTo
import io.primer.android.components.ui.assets.Brand
import io.primer.android.data.token.model.ClientTokenIntent
import kotlinx.serialization.Serializable

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
    PRIMER_TEST_KLARNA(brand = Brand.KLARNA),
    GOOGLE_PAY(brand = Brand.GOOGLE_PAY),
    PAYPAL(brand = Brand.PAYPAL),
    PRIMER_TEST_PAYPAL(brand = Brand.PAYPAL),
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
    PRIMER_TEST_SOFORT(brand = Brand.SOFORT),
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
    COINBASE(ClientTokenIntent.COINBASE_REDIRECTION, Brand.COINBASE),
    TWOC2P(ClientTokenIntent.TWOC2P_REDIRECTION, Brand.TWOC2P),
    OPENNODE(ClientTokenIntent.OPENNODE_REDIRECTION, Brand.OPENNODE),
    RAPYD_GCASH(ClientTokenIntent.RAPYD_GCASH_REDIRECTION, Brand.GCASH),

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    UNKNOWN(brand = Brand.UNKNOWN);

    companion object {
        fun safeValueOf(type: String?) = values().find { type == it.name }
            ?: UNKNOWN
    }

    constructor(intent: ClientTokenIntent, brand: Brand) : this(arrayOf(intent), brand)
}

internal fun PaymentMethodType.isAvailableOnHUC(): Boolean {
    return when (this) {
        PaymentMethodType.PAYMENT_CARD,
        PaymentMethodType.KLARNA,
        PaymentMethodType.GOOGLE_PAY,
        PaymentMethodType.PAYPAL,
        PaymentMethodType.GOCARDLESS,
        PaymentMethodType.APAYA,
        PaymentMethodType.PAY_NL_P24,
        PaymentMethodType.PAY_NL_IDEAL,
        PaymentMethodType.PAY_NL_PAYCONIQ,
        PaymentMethodType.PAY_NL_GIROPAY,
        PaymentMethodType.PAY_NL_EPS,
        PaymentMethodType.HOOLAH,
        PaymentMethodType.ADYEN_GIROPAY,
        PaymentMethodType.ADYEN_TWINT,
        PaymentMethodType.ADYEN_SOFORT,
        PaymentMethodType.ADYEN_TRUSTLY,
        PaymentMethodType.ADYEN_ALIPAY,
        PaymentMethodType.ADYEN_VIPPS,
        PaymentMethodType.ADYEN_MOBILEPAY,
        PaymentMethodType.ADYEN_PAYTRAIL,
        PaymentMethodType.ADYEN_INTERAC,
        PaymentMethodType.ADYEN_PAYSHOP,
        PaymentMethodType.MOLLIE_BANCONTACT,
        PaymentMethodType.MOLLIE_IDEAL,
        PaymentMethodType.MOLLIE_P24,
        PaymentMethodType.MOLLIE_GIROPAY,
        PaymentMethodType.MOLLIE_EPS,
        PaymentMethodType.BUCKAROO_GIROPAY,
        PaymentMethodType.BUCKAROO_SOFORT,
        PaymentMethodType.BUCKAROO_IDEAL,
        PaymentMethodType.BUCKAROO_EPS,
        PaymentMethodType.BUCKAROO_BANCONTACT,
        PaymentMethodType.COINBASE,
        PaymentMethodType.TWOC2P,
        PaymentMethodType.OPENNODE,
        PaymentMethodType.ATOME,
        PaymentMethodType.RAPYD_GCASH,
        PaymentMethodType.PRIMER_TEST_KLARNA,
        PaymentMethodType.PRIMER_TEST_PAYPAL,
        PaymentMethodType.PRIMER_TEST_SOFORT -> true
        PaymentMethodType.ADYEN_IDEAL,
        PaymentMethodType.ADYEN_DOTPAY,
        PaymentMethodType.ADYEN_BLIK,
        PaymentMethodType.ADYEN_MBWAY,
        PaymentMethodType.ADYEN_BANK_TRANSFER,
        PaymentMethodType.XFERS_PAYNOW,
        PaymentMethodType.UNKNOWN -> false
    }
}
