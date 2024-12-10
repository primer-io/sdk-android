package io.primer.android.nolpay.implementation.linkCard.domain.model

import io.primer.android.core.domain.Params

internal data class NolPayLinkCardOTPParams(
    val mobileNumber: String,
    val countryCallingCode: String,
    val linkToken: String
) : Params
