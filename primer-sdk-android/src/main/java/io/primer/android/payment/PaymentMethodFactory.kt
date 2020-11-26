package io.primer.android.payment

import android.view.ViewGroup
import io.primer.android.payment.card.CreditCard
import io.primer.android.ui.PrimerViewModel

class PaymentMethodFactory(viewModel: PrimerViewModel) {
  private val viewModel = viewModel

  fun create(config: PaymentMethodRemoteConfig): PaymentMethod? {
    // TODO: Clean this shit up - there must be a better way to do this

    return when (config.type) {
      "PAYMENT_CARD" -> CreditCard(viewModel)
      else -> null
    }
  }
}