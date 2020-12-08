package io.primer.android.payment

import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodToken(
  val id: String
)