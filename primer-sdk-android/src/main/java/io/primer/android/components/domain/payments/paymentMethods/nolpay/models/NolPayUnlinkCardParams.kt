package io.primer.android.components.domain.payments.paymentMethods.nolpay.models

import io.primer.android.domain.base.Params

class NolPayUnlinkCardParams(
    val unlinkToken: String,
    val cardNumber: String,
    val otpCode: String
) : Params
