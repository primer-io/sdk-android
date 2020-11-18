package io.primer.android.payment_methods

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.util.*

@Serializable
data class PaymentMethodRemoteConfig(
  val id: String? = null,
  val type: String,
  val options: JsonObject? = JsonObject(content = Collections.emptyMap())
)