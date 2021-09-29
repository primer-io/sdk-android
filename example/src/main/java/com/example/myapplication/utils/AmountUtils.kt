package com.example.myapplication.utils

import com.example.myapplication.datamodels.CurrencyCode
import java.util.*
import kotlin.math.pow

object AmountUtils {

    private const val AMOUNT_DIVIDER = 100

    fun covert(amount: Int, currency: CurrencyCode): Int {
        return (amount * 10.0.pow(Currency.getInstance(currency.name).defaultFractionDigits) / AMOUNT_DIVIDER).toInt()
    }
}