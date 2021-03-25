package io.primer.android.model.dto

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
internal class MonetaryAmount private constructor(
    val value: Int,
    val currency: String,
) {

    companion object {

        // FIXME it doesn't make sense to have a static factory method returning a null
        fun create(currency: String? = null, value: Int? = null): MonetaryAmount? {
            if (currency == null || value == null) {
                return null
            }

            if (value <= 0) {
                // TODO: handle invalid input
                return null
            }

            return try {
                MonetaryAmount(value, Currency.getInstance(currency).currencyCode)
            } catch (e: IllegalArgumentException) {
                // TODO: handle invalid currency code
                null
            }
        }
    }
}
