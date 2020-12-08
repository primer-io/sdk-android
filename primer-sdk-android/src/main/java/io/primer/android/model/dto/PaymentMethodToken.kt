package io.primer.android.model.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class PaymentMethodToken(
  val token: String,
  val analyticsId: String,
  val tokenType: TokenType,
  val paymentInstrumentType: String,
  val paymentInstrumentData: JsonObject,
  val vaultData: VaultData?
) {

  @Serializable
  data class VaultData(
    val customerId: String
  )
}