package io.primer.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import io.primer.android.R
import io.primer.android.UniversalCheckout
import io.primer.android.payment.CurrencyFormatter
import io.primer.android.payment.MonetaryAmount

internal class SelectPaymentMethodTitle(context: Context, attrs: AttributeSet? = null): LinearLayout(context, attrs) {
  private var uxMode: UniversalCheckout.UXMode? = null
  private var amount: MonetaryAmount? = null

  fun setUXMode(uxMode: UniversalCheckout.UXMode) {
    this.uxMode = uxMode
    update()
  }

  fun setAmount(amount: MonetaryAmount?) {
    this.amount = amount
    update()
  }

  private fun update() {
    when (uxMode) {
      UniversalCheckout.UXMode.CHECKOUT -> {
        findViewById<TextView>(R.id.primer_sheet_title).setText(R.string.prompt_pay)

        amount.let { amt ->
          val amount = CurrencyFormatter.format(amt)

          if (amount == null) {
            findViewById<TextView>(R.id.primer_sheet_title_detail).visibility = View.GONE
          } else {
            findViewById<TextView>(R.id.primer_sheet_title_detail).text = amount
          }
        }
      }
      UniversalCheckout.UXMode.ADD_PAYMENT_METHOD -> {
        findViewById<TextView>(R.id.primer_sheet_title).setText(R.string.prompt_add_new_card)
        findViewById<TextView>(R.id.primer_sheet_title_detail).visibility = View.GONE

      }
    }
  }
}