package io.primer.android

import kotlinx.serialization.Serializable

@Serializable
abstract sealed class PaymentMethod(val identifier: String) {

  @Serializable
  data class Card(val cardholderNameRequired: Boolean = true): io.primer.android.PaymentMethod(
    "PAYMENT_CARD"
  )

  @Serializable
  data class PayPal(val buttonColor: String = "gold"): io.primer.android.PaymentMethod(
    "PAYPAL"
  )

  @Serializable
  data class GooglePay(val buttonColor: String = "black") : io.primer.android.PaymentMethod(
    "GOOGLE_PAY"
  )
}