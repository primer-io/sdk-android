package io.primer.android

import io.primer.android.payment.MonetaryAmount
import kotlinx.serialization.Serializable

@Serializable
data class CheckoutConfig private constructor(
  val clientToken: String,
  val amount: MonetaryAmount? = null,
) {
  companion object {
    fun create(
      clientToken: String,
      currency: String? = null,
      amount: Int? = null
    ): CheckoutConfig {
      return CheckoutConfig(
        clientToken = clientToken,
        amount = MonetaryAmount.create(currency = currency, value = amount)
      )
    }
  }
}