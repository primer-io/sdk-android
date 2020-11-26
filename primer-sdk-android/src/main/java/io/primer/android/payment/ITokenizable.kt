package io.primer.android.payment

import org.json.JSONObject

interface ITokenizable {
  fun toPaymentInstrument(): JSONObject
}