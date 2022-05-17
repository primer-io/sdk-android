package io.primer.android.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.R
import io.primer.android.PaymentMethodIntent
import io.primer.android.di.DIAppComponent
import io.primer.android.model.MonetaryAmount
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject

@KoinApiExtension
internal class SelectPaymentMethodTitle(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs), DIAppComponent {

    private val theme: PrimerTheme by inject()

    private var paymentMethodIntent: PaymentMethodIntent? = null
    private var amount: MonetaryAmount? = null

    fun setUxMode(paymentMethodIntent: PaymentMethodIntent) {
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
                PaymentMethodIntent.CHECKOUT -> {
                    PayAmountText.generate(context, amount)
                }
                PaymentMethodIntent.VAULT -> "" // this is for displaying amount, title sits above
                else -> ""
            }
            setTextColor(theme.amountLabelText.defaultColor.getColor(context, theme.isDarkMode))
        }
    }
}
