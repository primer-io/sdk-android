package io.primer.android.nolpay.implementation.unlinkCard.domain.model

import io.primer.android.core.domain.Params

internal data class NolPayUnlinkCardParams(
    val cardNumber: String,
    val otpCode: String,
    val unlinkToken: String
) : Params
