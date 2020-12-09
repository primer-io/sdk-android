package io.primer.android.payment

import androidx.annotation.DrawableRes
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PaymentMethodToken
import kotlinx.serialization.json.JsonObject

internal abstract class TokenAttributes private constructor(token: PaymentMethodToken, @DrawableRes val icon: Int) {
  val id = token.token

  protected val data = token.paymentInstrumentData

  abstract val description: String

  internal class PaymentCardAttributes(token: PaymentMethodToken) : TokenAttributes(token, R.drawable.credit_card_icon) {
    private val log = Logger("payment-card")

    override val description: String
      get() {
        log(data.toString())
        return "Mastercard ●●●●1234"
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