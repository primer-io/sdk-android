package io.primer.android.payment

import org.json.JSONObject

interface ITokenizable {
  fun setValue(key: String, value: String)
  fun toPaymentInstrument(): JSONObject
}