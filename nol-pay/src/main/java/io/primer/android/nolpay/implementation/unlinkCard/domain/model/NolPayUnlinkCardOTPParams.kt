package io.primer.android.nolpay.implementation.unlinkCard.domain.model

import io.primer.android.core.domain.Params

internal data class NolPayUnlinkCardOTPParams(
    val mobileNumber: String,
    val countryCallingCode: String,
    val cardNumber: String
) : Params
