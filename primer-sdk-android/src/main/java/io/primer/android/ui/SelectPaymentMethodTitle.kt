package io.primer.android.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import io.primer.android.PrimerSessionIntent
import io.primer.android.R
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.inject
import io.primer.android.model.MonetaryAmount
import io.primer.android.ui.settings.PrimerTheme

internal class SelectPaymentMethodTitle(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs), DISdkComponent {

    private val theme: PrimerTheme by inject()

    private var paymentMethodIntent: PrimerSessionIntent? = null
    private var amount: MonetaryAmount? = null

    fun setUxMode(paymentMethodIntent: PrimerSessionIntent) {
        this.paymentMethodIntent = paymentMethodIntent
        update()
    }

    fun setAmount(amount: MonetaryAmount?) {
        this.amount = amount
        update()
    }

    private fun update() {
        findViewById<TextView>(R.id.primer_sheet_title).apply {
            text = when (paymentMethodIntent) {
                PrimerSessionIntent.CHECKOUT -> {
                    PayAmountText.generate(context, amount)
                }
                PrimerSessionIntent.VAULT ->
                    "" // this is for displaying amount, title sits above
                else -> ""
            }
            setTextColor(theme.amountLabelText.defaultColor.getColor(context, theme.isDarkMode))
        }
    }
}
