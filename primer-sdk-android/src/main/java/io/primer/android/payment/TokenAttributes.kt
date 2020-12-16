package io.primer.android.payment

import android.content.Context
import androidx.annotation.DrawableRes
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PaymentMethodToken
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal abstract class TokenAttributes private constructor(
  token: PaymentMethodToken,
  @DrawableRes val icon: Int
) {
  val id = token.token

  protected val data = token.paymentInstrumentData

  abstract fun getDescription(context: Context): String

  internal class PaymentCardAttributes(token: PaymentMethodToken) :
    TokenAttributes(token, R.drawable.credit_card_icon) {
    private val log = Logger("payment-card")

    override fun getDescription(context: Context): String {
      val network = data["network"]?.jsonPrimitive?.contentOrNull
        ?: context.getString(R.string.card_network_fallback)
      val digits = data["last4Digits"]?.jsonPrimitive?.contentOrNull ?: ""
      var description = network

      if (digits.isNotEmpty()) {
        description += " ●●●●$digits"
      }

      return description.trim()
    }
  }

  companion object {
    fun create(token: PaymentMethodToken): TokenAttributes? {
      // TODO: hate this - change it
      return when (token.paymentInstrumentType) {
        PAYMENT_CARD_TYPE -> PaymentCardAttributes(token)
        else -> null
      }
    }
  }
}