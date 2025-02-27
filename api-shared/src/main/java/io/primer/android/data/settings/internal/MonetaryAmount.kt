package io.primer.android.data.settings.internal

import java.util.Currency

class MonetaryAmount private constructor(
    val value: Int,
    val currency: String,
) {
    companion object {
        // FIXME it doesn't make sense to have a static factory method returning a null
        fun create(
            currency: String? = null,
            value: Int? = null,
        ): MonetaryAmount? =
            when {
                currency == null || value == null -> null
                value < 0 -> null // FIXME handle invalid input
                else ->
                    try {
                        MonetaryAmount(value, Currency.getInstance(currency).currencyCode)
                    } catch (ignored: IllegalArgumentException) {
                        // FIXME handle invalid currency code
                        null
                    }
            }
    }
}
