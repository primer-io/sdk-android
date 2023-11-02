package io.primer.android.components.domain.payments.paymentMethods.nolpay.models

import io.primer.android.domain.base.Params

internal data class NolPayGetLinkedCardsParams(
    val mobileNumber: String,
    val countryCallingCode: String
) : Params
