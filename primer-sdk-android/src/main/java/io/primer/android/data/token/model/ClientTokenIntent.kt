package io.primer.android.data.token.model

internal enum class ClientTokenIntent {

    CHECKOUT,
    `3DS_AUTHENTICATION`,
    PAY_NL_IDEAL_REDIRECTION,
    PAY_NL_PAYCONIQ_REDIRECTION,
    PAY_NL_GIROPAY_REDIRECTION,
    HOOLAH_REDIRECTION,
    ADYEN_GIROPAY_REDIRECTION,
    ADYEN_TWINT_REDIRECTION,
    ADYEN_SOFORT_REDIRECTION,
    ADYEN_TRUSTLY_REDIRECTION,
    ADYEN_ALIPAY_REDIRECTION,
    ADYEN_VIPPS_REDIRECTION,
    ADYEN_MOBILEPAY_REDIRECTION,
    ADYEN_IDEAL_REDIRECTION,
    ADYEN_DOTPAY_REDIRECTION,
    MOLLIE_BANCONTACT_REDIRECTION,
    MOLLIE_IDEAL_REDIRECTION
}
