package io.primer.android.ui

import android.content.Context
import io.primer.android.R
import io.primer.android.UniversalCheckout
import io.primer.android.payment.CurrencyFormatter
import io.primer.android.payment.MonetaryAmount

internal class PayAmountText {
  companion object {
    fun generate(context: Context, uxMode: UniversalCheckout.UXMode?, amount: MonetaryAmount?): String {
      return when (uxMode) {
        UniversalCheckout.UXMode.CHECKOUT -> {
          return context.getString(R.string.pay_amount, CurrencyFormatter.format(amount) ?: "")
        }
        UniversalCheckout.UXMode.ADD_PAYMENT_METHOD -> {
          return context.getString(R.string.add_new_card_hint)
        }
        null -> ""
      }
    }
  }
}