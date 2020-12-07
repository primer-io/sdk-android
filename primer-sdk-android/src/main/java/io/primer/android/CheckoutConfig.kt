package io.primer.android

import io.primer.android.payment.MonetaryAmount
import kotlinx.serialization.Serializable

@Serializable
internal data class CheckoutConfig private constructor(
  val clientToken: String,
  val uxMode: UniversalCheckout.UXMode,
  val amount: MonetaryAmount? = null,
) {
  companion object {
    fun create(
      clientToken: String,
      uxMode: UniversalCheckout.UXMode = UniversalCheckout.UXMode.CHECKOUT,
      currency: String? = null,
      amount: Int? = null
    ): CheckoutConfig {
      return CheckoutConfig(
        clientToken,
        uxMode = uxMode,
        amount = MonetaryAmount.create(currency = currency, value = amount)
      )
    }
  }
}