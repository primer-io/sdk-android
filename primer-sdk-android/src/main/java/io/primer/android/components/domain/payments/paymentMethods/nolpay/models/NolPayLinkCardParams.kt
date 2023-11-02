package io.primer.android.components.domain.payments.paymentMethods.nolpay.models

import io.primer.android.domain.base.Params

internal data class NolPayLinkCardParams(val linkToken: String, val otpCode: String) : Params
