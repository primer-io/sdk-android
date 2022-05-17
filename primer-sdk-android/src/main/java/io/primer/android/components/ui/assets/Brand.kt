package io.primer.android.components.ui.assets

import androidx.annotation.DrawableRes
import io.primer.android.R

internal enum class Brand(
    @DrawableRes internal val iconResId: Int,
    @DrawableRes internal val logoResId: Int = 0
) {
    PAYPAL(R.drawable.ic_logo_paypal, R.drawable.ic_logo_paypal_square),
    GOOGLE_PAY(R.drawable.ic_logo_googlepay, R.drawable.ic_logo_google_pay_square),
    KLARNA(R.drawable.ic_logo_klarna, R.drawable.ic_logo_klarna_square),
    APAYA(R.drawable.ic_logo_apaya, R.drawable.ic_logo_apaya),
    PAYMENT_CARD(R.drawable.ic_logo_credit_card),
    GOCARDLESS(R.drawable.ic_logo_gocardless),
    IDEAL(R.drawable.ic_logo_ideal, R.drawable.ic_logo_ideal_square),
    PAYQONIC(R.drawable.ic_logo_payconiq, R.drawable.ic_logo_payconiq_square),
    GIROPAY(R.drawable.ic_logo_giropay, R.drawable.ic_logo_giropay_square),
    P24(R.drawable.ic_logo_p24, R.drawable.ic_logo_p24_square),
    EPS(R.drawable.ic_logo_eps, R.drawable.ic_logo_eps_square),
    HOOLAH(R.drawable.ic_logo_hoolah, R.drawable.ic_logo_hoolah_square),
    TWINT(R.drawable.ic_logo_twint, R.drawable.ic_logo_twint_square),
    SOFORT(R.drawable.ic_logo_sofort, R.drawable.ic_logo_sofort_square),
    TRUSTLY(R.drawable.ic_logo_trusly, R.drawable.ic_logo_trustly_square),
    ALIPAY(R.drawable.ic_logo_alipay, R.drawable.ic_logo_alipay_square),
    VIPPS(R.drawable.ic_logo_vipps, R.drawable.ic_logo_vipps_square),
    MOBILEPAY(R.drawable.ic_logo_mobilepay, R.drawable.ic_logo_mobilepay_square),
    DOTPAY(R.drawable.ic_logo_dotpay_dark),
    BLIK(R.drawable.ic_logo_blik, R.drawable.ic_logo_blik_square),
    MBWAY(R.drawable.ic_logo_mbway, R.drawable.ic_logo_mbway),
    BANK_TRANSFER(R.drawable.ic_logo_sepa, R.drawable.ic_logo_sepa_square),
    BANCONTACT(R.drawable.ic_logo_bancontact, R.drawable.ic_logo_bancontact_square),
    ATOME(R.drawable.ic_logo_atome, R.drawable.ic_logo_atome_square),
    PAYNOW(R.drawable.ic_logo_xfers, R.drawable.ic_logo_xfers_square),
    INTERAC(R.drawable.ic_logo_interac, R.drawable.ic_logo_interac_square),
    PAYTRAIL(R.drawable.ic_logo_paytrail_light, R.drawable.ic_logo_paytrail_square),
    PAYSHOP(R.drawable.ic_logo_payshop_light, R.drawable.ic_logo_payshop_square),
    COINBASE(R.drawable.ic_coinbase_logo, R.drawable.ic_coinbase_logo_square),
    TWOC2P(R.drawable.ic_2c2p_logo, R.drawable.ic_2c2p_logo_square),
    UNKNOWN(0)
}

enum class ImageType {
    LOGO, ICON
}

enum class ImageColor {
    ORIGINAL, LIGHT, DARK
}
