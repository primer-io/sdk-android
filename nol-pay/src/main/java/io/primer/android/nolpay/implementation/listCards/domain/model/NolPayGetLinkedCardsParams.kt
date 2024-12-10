package io.primer.android.nolpay.implementation.listCards.domain.model

import io.primer.android.core.domain.Params

internal data class NolPayGetLinkedCardsParams(
    val mobileNumber: String,
    val countryCallingCode: String
) : Params
