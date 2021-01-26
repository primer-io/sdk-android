package io.primer.android.payment

import android.content.Context
import androidx.annotation.DrawableRes
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PaymentMethodToken
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal abstract class TokenAttributes private constructor(
  token: PaymentMethodToken,
  @DrawableRes val icon: Int,
  val iconScale: Float = 1.0f
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

  internal class PayPalBillingAgreementAttributes(token: PaymentMethodToken) :
    TokenAttributes(token, R.drawable.icon_paypal_sm) {
    private val log = Logger("paypal")

    override fun getDescription(context: Context): String {
      return data["externalPayerInfo"]?.jsonObject?.get("email")?.jsonPrimitive?.contentOrNull
        ?: "PayPal"
    }
  }

  internal class GoCardlessMandateAttributes(token: PaymentMethodToken) :
    TokenAttributes(token, R.drawable.icon_bank) {
    private val log = Logger("go-cardless")
    override fun getDescription(context: Context): String {
      val ref = data["gocardlessMandateId"]?.jsonPrimitive?.contentOrNull ?: ""
      return context.getString(R.string.bank_account) + " $ref"
    }
  }

  companion object {
    fun create(token: PaymentMethodToken): TokenAttributes? {
      // TODO: hate this - change it
      return when (token.paymentInstrumentType) {
        PAYMENT_CARD_TYPE -> PaymentCardAttributes(token)
        PAYPAL_BILLING_AGREEMENT_TYPE -> PayPalBillingAgreementAttributes(token)
        GOCARDLESS_MANDATE_TYPE -> GoCardlessMandateAttributes(token)
        else -> null
      }
    }
  }
}