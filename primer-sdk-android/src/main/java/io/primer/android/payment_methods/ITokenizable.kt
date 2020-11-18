package io.primer.android.payment_methods

import org.json.JSONObject

interface ITokenizable {
  fun toPaymentInstrument(): JSONObject
}