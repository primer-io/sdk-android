package io.primer.android.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import io.primer.android.R
import io.primer.android.UXMode
import io.primer.android.model.dto.MonetaryAmount

internal class SelectPaymentMethodTitle(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private var uxMode: UXMode? = null
    private var amount: MonetaryAmount? = null

    fun setUXMode(uxMode: UXMode) {
        this.uxMode = uxMode
        update()
    }

    fun setAmount(amount: MonetaryAmount?) {
        this.amount = amount
        update()
    }

    private fun update() {
        findViewById<TextView>(R.id.primer_sheet_title).text = when (uxMode) {
            UXMode.CHECKOUT -> PayAmountText.generate(context, amount)
            UXMode.VAULT -> context.getString(R.string.add_new_payment_method)
            else -> ""
        }
    }
}
