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

  @Serializable
  class GoCardless(
    val companyName: String,
    val companyAddress: String,
    val customerName: String? = null,
    val customerEmail: String? = null,
    val customerAddressLine1: String? = null,
    val customerAddressLine2: String? = null,
    val customerAddressCity: String? = null,
    val customerAddressState: String? = null,
    val customerAddressCountryCode: String? = null,
    val customerAddressPostalCode: String? = null,
  ) : io.primer.android.PaymentMethod(
    GOCARDLESS_IDENTIFIER
  )
}