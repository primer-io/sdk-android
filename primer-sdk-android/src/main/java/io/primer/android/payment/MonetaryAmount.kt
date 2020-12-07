package io.primer.android.payment

import kotlinx.serialization.Serializable
import java.lang.IllegalArgumentException
import java.util.*

@Serializable
internal class MonetaryAmount private constructor(
  val value: Int,
  val currency: String
) {
  companion object {
    fun create(currency: String? = null, value: Int? = null): MonetaryAmount?  {
      if (currency == null || value == null) {
        return null
      }

      if (value <= 0) {
        // TODO: handle invalid input
        return null
      }

      try {
        return MonetaryAmount(value, Currency.getInstance(currency).currencyCode)
      } catch (e: IllegalArgumentException) {
        // TODO: handle invalid currency code
        return null
      }
    }
  }
}