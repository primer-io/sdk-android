package io.primer.android.components.domain.payments.paymentMethods.nolpay.models

import io.primer.android.domain.base.Params

internal data class NolPayLinkCardOTPParams(
    val mobileNumber: String,
    val countryCallingCode: String,
    val linkToken: String
) : Params
