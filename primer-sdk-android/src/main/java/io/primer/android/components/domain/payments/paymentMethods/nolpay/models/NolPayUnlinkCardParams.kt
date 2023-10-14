package io.primer.android.components.domain.payments.paymentMethods.nolpay.models

import io.primer.android.domain.base.Params

internal data class NolPayUnlinkCardParams(
    val cardNumber: String,
    val otpCode: String,
    val unlinkToken: String
) : Params
