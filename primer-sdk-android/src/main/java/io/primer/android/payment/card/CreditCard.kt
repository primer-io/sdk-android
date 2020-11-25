package io.primer.android.payment.card

import io.primer.android.payment.PaymentMethod
import io.primer.android.session.ClientSession
import org.json.JSONObject
import kotlin.collections.HashMap

class CreditCard(session: ClientSession) : PaymentMethod(session) {
  override val id = "PAYMENT_CARD"

  override val isVaultable: Boolean
    get() = true

  private var values: MutableMap<String, String> = HashMap();

  override fun setValue(key: String, value: String) {
    values[key] = value
  }

  override fun toPaymentInstrument(): JSONObject {
    return JSONObject(values.toMap())
  }
}