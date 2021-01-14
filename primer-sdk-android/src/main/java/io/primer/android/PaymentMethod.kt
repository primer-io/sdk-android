package io.primer.android

import kotlinx.serialization.Serializable

@Serializable
sealed class PaymentMethod(val identifier: String) {
  @Serializable
  class Card : io.primer.android.PaymentMethod(
    PAYMENT_CARD_IDENTIFIER
  )

  @Serializable
  class PayPal : io.primer.android.PaymentMethod(
    PAYPAL_IDENTIFIER
  )
}