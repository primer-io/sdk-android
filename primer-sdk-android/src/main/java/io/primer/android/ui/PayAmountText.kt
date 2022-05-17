package io.primer.android.ui

import android.content.Context
import io.primer.android.R
import io.primer.android.model.MonetaryAmount
import io.primer.android.utils.PaymentUtils

// FIXME this should not be an object/static (we can probably remove it as it doesn't do much)
internal object PayAmountText {

    fun generate(context: Context, amount: MonetaryAmount?): String {
        val payAmount = PaymentUtils.amountToCurrencyString(amount) ?: ""
        return context.getString(R.string.pay_amount, payAmount)
    }
}
