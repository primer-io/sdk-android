package io.primer.android

import kotlinx.serialization.Serializable

@Serializable
sealed class PaymentMethod(val identifier: String) {

  @Serializable
  data class Card(val cardholderNameRequired: Boolean = true): io.primer.android.PaymentMethod(
    PAYMENT_CARD_IDENTIFIER
  )

  @Serializable
  data class PayPal(val buttonColor: String = "gold"): io.primer.android.PaymentMethod(
    PAYPAL_IDENTIFIER
  )

  @Serializable
  data class GooglePay(val buttonColor: String = "black") : io.primer.android.PaymentMethod(
    GOOGLE_PAY_IDENTIFIER
  )
}