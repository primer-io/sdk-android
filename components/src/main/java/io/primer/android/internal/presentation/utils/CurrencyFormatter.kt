package io.primer.android.internal.presentation.utils

import android.content.Context
import io.primer.android.components.R
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfo
import java.text.NumberFormat
import java.util.Currency

object CurrencyFormatter {

    fun formatAmount(amountInCents: Int, currencyCode: String): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val formatter = NumberFormat.getCurrencyInstance()
            formatter.currency = currency
            formatter.format(amountInCents / 100.0)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatTitle(context: Context, orderInfo: BasicOrderInfo): String {
        val amount = formatAmount(orderInfo.totalAmount, orderInfo.currencyCode)
        return if (amount.isNotEmpty()) {
            context.getString(R.string.primer_components_payment_method_selection_pay_amount, amount)
        } else {
            context.getString(R.string.primer_components_payment_method_selection_pay)
        }
    }
}
