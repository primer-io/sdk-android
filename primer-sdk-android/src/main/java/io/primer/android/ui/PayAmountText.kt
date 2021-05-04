package io.primer.android.ui

import android.content.Context
import io.primer.android.R
import io.primer.android.model.dto.MonetaryAmount

internal object PayAmountText {

    fun generate(context: Context, amount: MonetaryAmount?): String {
        return context.getString(R.string.pay_amount, CurrencyFormatter.format(amount) ?: "")
    }
}
