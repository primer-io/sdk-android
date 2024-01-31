package io.primer.android.domain.currencyformat.models

import io.primer.android.domain.base.Params
import io.primer.android.model.MonetaryAmount

internal data class FormatCurrencyParams(
    val amount: MonetaryAmount
) : Params
