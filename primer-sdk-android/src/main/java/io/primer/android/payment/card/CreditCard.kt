package io.primer.android.payment.card

import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.payment.PaymentMethod
import io.primer.android.ui.PrimerViewModel
import org.json.JSONObject
import kotlin.collections.HashMap

class CreditCard(viewModel: PrimerViewModel) : PaymentMethod(viewModel) {
  override val id = "PAYMENT_CARD"

  override val isVaultable: Boolean
    get() = true

  private var values: MutableMap<String, String> = HashMap();

  override fun toPaymentInstrument(): JSONObject {
    return JSONObject(values.toMap())
  }

  override fun renderPreview(container: ViewGroup) {
    View.inflate(container.context, R.layout.pm_credit_card_layout, container)
  }
}