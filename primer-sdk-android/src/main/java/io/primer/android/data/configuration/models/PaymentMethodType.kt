package io.primer.android.data.configuration.models

import androidx.annotation.RestrictTo
import io.primer.android.components.ui.assets.Brand
import io.primer.android.components.ui.assets.ImageColor
import io.primer.android.data.token.model.ClientTokenIntent

internal enum class PaymentMethodType(
    internal val intents: Array<ClientTokenIntent>? = null,
    internal val brand: Brand
) {
    PAYMENT_CARD(
        arrayOf(
            ClientTokenIntent.`3DS_AUTHENTICATION`,
            ClientTokenIntent.PROCESSOR_3DS
        ),
        Brand.PAYMENT_CARD
    ),
    KLARNA(brand = Brand.KLARNA),
    PRIMER_TEST_KLARNA(brand = Brand.KLARNA),
    GOOGLE_PAY(ClientTokenIntent.`3DS_AUTHENTICATION`, Brand.GOOGLE_PAY),
    PAYPAL(brand = Brand.PAYPAL),
    PRIMER_TEST_PAYPAL(brand = Brand.PAYPAL),
    APAYA(brand = Brand.APAYA),
    PAY_NL_IDEAL(ClientTokenIntent.PAY_NL_IDEAL_REDIRECTION, Brand.IDEAL),
    PAY_NL_PAYCONIQ(ClientTokenIntent.PAY_NL_PAYCONIQ_REDIRECTION, Brand.PAYQONIC),
    PAY_NL_GIROPAY(ClientTokenIntent.PAY_NL_GIROPAY_REDIRECTION, Brand.GIROPAY),
    PAY_NL_P24(ClientTokenIntent.PAY_NL_P24_REDIRECTION, Brand.P24),
    PAY_NL_EPS(ClientTokenIntent.PAY_NL_EPS_REDIRECTION, Brand.EPS),
    HOOLAH(ClientTokenIntent.HOOLAH_REDIRECTION, Brand.HOOLAH),
    ADYEN_TWINT(ClientTokenIntent.ADYEN_TWINT_REDIRECTION, Brand.TWINT),
    ADYEN_SOFORT(ClientTokenIntent.ADYEN_SOFORT_REDIRECTION, Brand.SOFORT),
    ADYEN_GIROPAY(ClientTokenIntent.ADYEN_GIROPAY_REDIRECTION, Brand.GIROPAY),
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
    ADYEN_BANCONTACT_CARD(ClientTokenIntent.ADYEN_BANCONTACT_CARD_REDIRECTION, Brand.BANCONTACT),
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
    TWOC2P(ClientTokenIntent.TWOC2P_REDIRECTION, Brand.TWOC2P),
    OPENNODE(ClientTokenIntent.OPENNODE_REDIRECTION, Brand.OPENNODE),
    RAPYD_GCASH(ClientTokenIntent.RAPYD_GCASH_REDIRECTION, Brand.GCASH),
    RAPYD_GRABPAY(ClientTokenIntent.RAPYD_GRABPAY_REDIRECTION, Brand.GRABPAY),
    RAPYD_POLI(ClientTokenIntent.RAPYD_POLI_REDIRECTION, Brand.POLI),
    RAPYD_FAST(ClientTokenIntent.RAPYD_FAST_REDIRECTION, Brand.FAST),
    RAPYD_PROMPTPAY(ClientTokenIntent.RAPYD_PROMPTPAY_REDIRECTION, Brand.PROMPTPAY),
    XENDIT_OVO(ClientTokenIntent.XENDIT_OVO_REDIRECTION, Brand.UNKNOWN),
    ADYEN_MULTIBANCO(ClientTokenIntent.PAYMENT_METHOD_VOUCHER, Brand.MULTIBANCO),
    OMISE_PROMPTPAY(ClientTokenIntent.OMISE_PROMPTPAY_REDIRECTION, Brand.PROMPTPAY),
    XENDIT_RETAIL_OUTLETS(ClientTokenIntent.PAYMENT_METHOD_VOUCHER, Brand.RETAIL_OUTLETS),
    IPAY88_CARD(ClientTokenIntent.IPAY88_CARD_REDIRECTION, Brand.IPAY_88),
    NOL_PAY(brand = Brand.UNKNOWN),

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    UNKNOWN(brand = Brand.UNKNOWN);

    companion object {
        fun safeValueOf(type: String?) = values().find { type == it.name }
            ?: UNKNOWN
    }

    constructor(intent: ClientTokenIntent, brand: Brand) : this(arrayOf(intent), brand)
}

internal fun Brand.getImageAsset(imageColor: ImageColor) = when (imageColor) {
    ImageColor.COLORED -> iconResId
    ImageColor.DARK -> iconDarkResId
    ImageColor.LIGHT -> iconLightResId
}
