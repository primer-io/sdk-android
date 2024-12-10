package io.primer.android.components.currencyformat.domain.models

import io.primer.android.data.settings.internal.MonetaryAmount
import io.primer.android.core.domain.Params

data class FormatCurrencyParams(
    val amount: MonetaryAmount
) : Params
