package io.primer.android.components.currencyformat.domain.models

import io.primer.android.core.domain.Params
import io.primer.android.data.settings.internal.MonetaryAmount

data class FormatCurrencyParams(
    val amount: MonetaryAmount,
) : Params
