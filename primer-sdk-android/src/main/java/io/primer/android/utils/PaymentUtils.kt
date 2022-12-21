package io.primer.android.utils

import io.primer.android.model.MonetaryAmount
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import kotlin.math.pow

internal object PaymentUtils {

    private const val AMOUNT_DIVIDER: Double = 10.0

    fun amountToCurrencyString(amount: MonetaryAmount?): String? {
        if (amount == null) {
            return null
        }

        val format = NumberFormat.getCurrencyInstance()
        val currency = Currency.getInstance(amount.currency)

        format.currency = currency
        format.maximumFractionDigits = currency.defaultFractionDigits
        format.minimumFractionDigits = currency.defaultFractionDigits

        return format.format(minorToAmount(amount.value, currency))
    }

    fun amountToDecimalString(amount: MonetaryAmount?): String? {
        if (amount == null) {
            return null
        }

        val format = NumberFormat.getCurrencyInstance()
        val currency = Currency.getInstance(amount.currency)

        format.currency = currency
        format.maximumFractionDigits = currency.defaultFractionDigits
        format.minimumFractionDigits = currency.defaultFractionDigits

        val decimalFormatSymbols: DecimalFormatSymbols =
            (format as DecimalFormat).decimalFormatSymbols

        decimalFormatSymbols.currencySymbol = ""

        format.decimalFormatSymbols = decimalFormatSymbols
        return format.format(minorToAmount(amount.value, currency))
    }

    fun minorToAmount(minorAmount: Int, currency: Currency): Double {
        return minorAmount.toDouble() / AMOUNT_DIVIDER.pow(currency.defaultFractionDigits)
    }
}
