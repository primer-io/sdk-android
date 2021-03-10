package io.primer.android.model.dto

import io.primer.android.UniversalCheckout
import io.primer.android.UniversalCheckoutTheme
import kotlinx.serialization.Serializable

@Serializable
internal data class CheckoutConfig private constructor(
  val clientToken: String,
  val uxMode: UniversalCheckout.UXMode,
  val theme: UniversalCheckoutTheme,
  val amount: MonetaryAmount? = null,
) {

    companion object {

        fun create(
          clientToken: String,
          uxMode: UniversalCheckout.UXMode = UniversalCheckout.UXMode.CHECKOUT,
          currency: String? = null,
          amount: Int? = null,
          theme: UniversalCheckoutTheme? = null,
        ): CheckoutConfig {
            return CheckoutConfig(
              clientToken,
              uxMode = uxMode,
              theme = theme ?: UniversalCheckoutTheme.getDefault(),
              amount = MonetaryAmount.create(currency = currency, value = amount)
            )
        }
    }
}
