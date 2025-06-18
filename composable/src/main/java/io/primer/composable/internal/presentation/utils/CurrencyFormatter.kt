package io.primer.composable.internal.presentation.utils

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
            "Pay"
        }
    }
    
    fun formatTitle(amountInCents: Int, currencyCode: String): String {
        return "Pay ${formatAmount(amountInCents, currencyCode)}"
    }
}
