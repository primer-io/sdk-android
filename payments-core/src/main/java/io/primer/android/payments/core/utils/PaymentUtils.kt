package io.primer.android.payments.core.utils

import java.util.Currency
import kotlin.math.pow

object PaymentUtils {
    private const val AMOUNT_DIVIDER: Double = 10.0

    fun minorToAmount(
        minorAmount: Int,
        currency: Currency,
    ): Double {
        return minorAmount.toDouble() / AMOUNT_DIVIDER.pow(currency.defaultFractionDigits)
    }

    fun minorToAmount(
        minorAmount: Int,
        fractionDigits: Int,
    ): Double {
        return minorAmount.toDouble() / AMOUNT_DIVIDER.pow(fractionDigits)
    }
}
