package io.primer.android.payment_methods.credit_card

import io.primer.android.payment_methods.PaymentMethod
import io.primer.android.session.ClientSession
import org.json.JSONObject
import java.util.*

class CreditCard(session: ClientSession) : PaymentMethod(session) {
  override var id = "PAYMENT_CARD"

  private var values: Map<String, String> = Collections.emptyMap();

  override fun toPaymentInstrument(): JSONObject {
    return JSONObject(values)
  }
}