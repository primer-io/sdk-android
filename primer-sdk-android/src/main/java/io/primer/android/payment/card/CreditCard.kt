package io.primer.android.payment.card

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.payment.PaymentMethod
import io.primer.android.ui.PrimerViewModel
import org.json.JSONObject
import kotlin.collections.HashMap

class CreditCard(viewModel: PrimerViewModel) : PaymentMethod(viewModel) {
  override val id = "PAYMENT_CARD"
  private val log = Logger("payment-method.$id")

  override val isVaultable: Boolean
    get() = true

  private var values: MutableMap<String, String> = HashMap();

  override fun toPaymentInstrument(): JSONObject {
    return JSONObject(values.toMap())
  }

  override fun renderPreview(container: ViewGroup) {
    View.inflate(container.context, R.layout.pm_credit_card_layout, container)

    val button = container.findViewById<View>(R.id.card_preview_button)

    button.setOnClickListener(this::onPreviewClicked)
  }

  private fun onPreviewClicked(view: View) {
    // TODO: Show the card form and collect details
  }
}