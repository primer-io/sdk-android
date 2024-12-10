package io.primer.android.nolpay.implementation.linkCard.domain.model

import io.primer.android.core.domain.Params

internal data class NolPayLinkCardParams(val linkToken: String, val otpCode: String) : Params
