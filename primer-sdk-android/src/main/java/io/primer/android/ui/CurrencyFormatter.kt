package io.primer.android.ui

import io.primer.android.model.dto.MonetaryAmount
import java.text.NumberFormat
import java.util.Currency

const val AMOUNT_DIVIDER: Int = 100

internal class CurrencyFormatter private constructor() {

    // FIXME this shouldn't be static
    companion object {

        fun format(amount: MonetaryAmount?): String? {
            if (amount == null) {
                return null
            }

            val format = NumberFormat.getCurrencyInstance()
            val currency = Currency.getInstance(amount.currency)

            format.currency = currency

            return format.format(amount.value.toDouble() / AMOUNT_DIVIDER)
        }
    }
}
