package io.primer.android.data.token.model

internal enum class ClientTokenIntent {

    CHECKOUT,
    `3DS_AUTHENTICATION`,
    PROCESSOR_3DS,
    PAY_NL_IDEAL_REDIRECTION,
    PAY_NL_PAYCONIQ_REDIRECTION,
    PAY_NL_GIROPAY_REDIRECTION,
    PAY_NL_P24_REDIRECTION,
    PAY_NL_EPS_REDIRECTION,
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
    ADYEN_BLIK_REDIRECTION,
    ADYEN_MBWAY_REDIRECTION,
    ADYEN_INTERAC_REDIRECTION,
    ADYEN_PAYTRAIL_REDIRECTION,
    ADYEN_PAYSHOP_REDIRECTION,
    MOLLIE_BANCONTACT_REDIRECTION,
    MOLLIE_IDEAL_REDIRECTION,
    MOLLIE_P24_REDIRECTION,
    MOLLIE_GIROPAY_REDIRECTION,
    MOLLIE_EPS_REDIRECTION,
    BUCKAROO_GIROPAY_REDIRECTION,
    BUCKAROO_SOFORT_REDIRECTION,
    BUCKAROO_IDEAL_REDIRECTION,
    BUCKAROO_EPS_REDIRECTION,
    BUCKAROO_BANCONTACT_REDIRECTION,
    ATOME_REDIRECTION,
    XFERS_PAYNOW_REDIRECTION,
    COINBASE_REDIRECTION,
    TWOC2P_REDIRECTION,
    OPENNODE_REDIRECTION,
    RAPYD_GCASH_REDIRECTION,
    RAPYD_GRABPAY_REDIRECTION,
    RAPYD_POLI_REDIRECTION,
    RAPYD_FAST_REDIRECTION,
    RAPYD_PROMPTPAY_REDIRECTION,
    XENDIT_OVO_REDIRECTION,
    PAYMENT_METHOD_VOUCHER,
}
