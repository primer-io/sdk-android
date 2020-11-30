package io.primer.android.payment

import android.view.View
import android.view.ViewGroup
import io.primer.android.PAYMENT_CARD_IDENTIFIER
import io.primer.android.payment.card.CreditCard
import io.primer.android.ui.PrimerViewModel

internal abstract class PaymentMethodDescriptor(
  protected val viewModel: PrimerViewModel,
  protected val config: PaymentMethodRemoteConfig): ITokenizable {

  abstract val identifier: String

  abstract val type: PaymentMethodType

  abstract val vaultCapability: VaultCapability

  abstract fun createButton(container: ViewGroup): View

  class Factory(private val viewModel: PrimerViewModel) {
    fun create(config: PaymentMethodRemoteConfig): PaymentMethodDescriptor? {
      return when (config.type) {
        PAYMENT_CARD_IDENTIFIER -> CreditCard(viewModel, config)
        else -> null
      }
    }
  }
}
