package io.primer.android.components.ui.assets

import androidx.annotation.DrawableRes
import io.primer.android.R

internal enum class Brand(
    @DrawableRes internal val iconResId: Int,
    @DrawableRes internal val logoResId: Int = 0,
    @DrawableRes internal val iconLightResId: Int = iconResId,
    @DrawableRes internal val iconDarkResId: Int = iconResId
) {
    PAYPAL(
        R.drawable.ic_logo_paypal,
        R.drawable.ic_logo_paypal_square
    ),
    GOOGLE_PAY(
        R.drawable.ic_logo_googlepay,
        R.drawable.ic_logo_google_pay_square,
        R.drawable.ic_logo_googlepay_light
    ),
    KLARNA(
        R.drawable.ic_logo_klarna,
        R.drawable.ic_logo_klarna_square,
        iconDarkResId = R.drawable.ic_logo_klarna_dark
    ),
    PAYMENT_CARD(R.drawable.ic_logo_credit_card),
    IDEAL(
        R.drawable.ic_logo_ideal,
        R.drawable.ic_logo_ideal_square
    ),
    PAYQONIC(
        R.drawable.ic_logo_payconiq,
        R.drawable.ic_logo_payconiq_square,
        R.drawable.ic_logo_payconiq_light
    ),
    GIROPAY(R.drawable.ic_logo_giropay, R.drawable.ic_logo_giropay_square),
    P24(R.drawable.ic_logo_p24, R.drawable.ic_logo_p24_square, R.drawable.ic_logo_p24_light),
    EPS(
        R.drawable.ic_logo_eps,
        R.drawable.ic_logo_eps_square,
        iconDarkResId = R.drawable.ic_logo_eps_dark
    ),
    HOOLAH(
        R.drawable.ic_logo_hoolah,
        R.drawable.ic_logo_hoolah_square,
        R.drawable.ic_logo_hoolah_light
    ),
    TWINT(
        R.drawable.ic_logo_twint,
        R.drawable.ic_logo_twint_square,
        R.drawable.ic_logo_twint_light
    ),
    SOFORT(
        R.drawable.ic_logo_sofort,
        R.drawable.ic_logo_sofort_square,
        R.drawable.ic_logo_sofort_light
    ),
    TRUSTLY(
        R.drawable.ic_logo_trusly,
        R.drawable.ic_logo_trustly_square,
        R.drawable.ic_logo_trustly_light
    ),
    ALIPAY(
        R.drawable.ic_logo_alipay,
        R.drawable.ic_logo_alipay_square,
        R.drawable.ic_logo_alipay_light
    ),
    VIPPS(
        R.drawable.ic_logo_vipps,
        R.drawable.ic_logo_vipps_square,
        R.drawable.ic_logo_vipps_light,
        R.drawable.ic_logo_vipps_light
    ),
    MOBILEPAY(
        R.drawable.ic_logo_mobilepay,
        R.drawable.ic_logo_mobilepay_square,
        R.drawable.ic_logo_mobilepay_light
    ),
    DOTPAY(R.drawable.ic_logo_dotpay_dark, iconLightResId = R.drawable.ic_logo_dotpay_light),
    BLIK(R.drawable.ic_logo_blik, R.drawable.ic_logo_blik_square, R.drawable.ic_logo_blik_light),
    MBWAY(
        R.drawable.ic_logo_mbway_light,
        iconDarkResId = R.drawable.ic_logo_mbway_dark
    ),
    BANK_TRANSFER(R.drawable.ic_logo_sepa, R.drawable.ic_logo_sepa_square),
    BANCONTACT(
        R.drawable.ic_logo_bancontact,
        R.drawable.ic_logo_bancontact_square,
        iconDarkResId = R.drawable.ic_logo_bancontact_dark
    ),
    ATOME(
        R.drawable.ic_logo_atome,
        R.drawable.ic_logo_atome_square,
        iconDarkResId = R.drawable.ic_logo_atome_dark
    ),
    PAYNOW(
        R.drawable.ic_logo_xfers,
        R.drawable.ic_logo_xfers_square,
        R.drawable.ic_logo_xfers_light
    ),
    INTERAC(
        R.drawable.ic_logo_interac,
        R.drawable.ic_logo_interac_square,
        iconDarkResId = R.drawable.ic_logo_interac_dark
    ),
    PAYTRAIL(
        R.drawable.ic_logo_paytrail,
        R.drawable.ic_logo_paytrail_square,
        R.drawable.ic_logo_paytrail_light
    ),
    PAYSHOP(
        R.drawable.ic_logo_payshop_dark,
        R.drawable.ic_logo_payshop_square,
        R.drawable.ic_logo_payshop_light,
        R.drawable.ic_logo_payshop_dark
    ),
    TWOC2P(
        R.drawable.ic_2c2p_logo,
        R.drawable.ic_2c2p_logo_square,
        iconDarkResId = R.drawable.ic_2c2p_logo_dark
    ),
    OPENNODE(R.drawable.ic_opennode_logo, R.drawable.ic_opennode_logo_square),
    GCASH(
        R.drawable.ic_logo_gcash_dark,
        R.drawable.ic_logo_gcash_square,
        R.drawable.ic_logo_gcash,
        R.drawable.ic_logo_gcash_dark
    ),
    GRABPAY(
        R.drawable.ic_logo_grab_pay,
        R.drawable.ic_opennode_logo_square,
        R.drawable.ic_logo_grab_pay_light,
        R.drawable.ic_logo_grab_pay_dark
    ),
    POLI(
        R.drawable.ic_logo_poli_dark,
        iconLightResId = R.drawable.ic_logo_poli_light,
        iconDarkResId = R.drawable.ic_logo_poli_dark
    ),
    FAST(R.drawable.ic_logo_fast_light, iconDarkResId = R.drawable.ic_logo_fast_dark),
    PROMPTPAY(
        R.drawable.ic_logo_promptpay_dark,
        iconLightResId = R.drawable.ic_logo_promptpay_light
    ),
    MULTIBANCO(
        R.drawable.ic_logo_multibanco_light,
        iconDarkResId = R.drawable.ic_logo_multibanco_dark
    ),
    RETAIL_OUTLETS(
        R.drawable.ic_retail_outlets,
        iconDarkResId = R.drawable.ic_retail_outlets_light
    ),
    IPAY_88(
        R.drawable.ic_logo_credit_card
    ),
    GENERIC_BANK(
        R.drawable.ic_bank_16
    ),
    UNKNOWN(0)
}

internal enum class ImageColor {
    COLORED, LIGHT, DARK
}
