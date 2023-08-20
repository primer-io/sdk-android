package io.primer.android.components.domain.payments.paymentMethods.nolpay.models

import io.primer.android.domain.base.Params

internal data class NolPayCardOTPParams(
    val mobileNumber: String,
    val phoneCountryCode: String,
    val linkToken: String
) : Params
