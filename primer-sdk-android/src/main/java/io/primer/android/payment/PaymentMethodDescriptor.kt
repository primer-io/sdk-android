package io.primer.android.payment

import android.view.View
import android.view.ViewGroup
import io.primer.android.GOOGLE_PAY_IDENTIFIER
import io.primer.android.PAYMENT_CARD_IDENTIFIER
import io.primer.android.PAYPAL_IDENTIFIER
import io.primer.android.PaymentMethod
import io.primer.android.payment.card.CreditCard
import io.primer.android.payment.googlepay.GooglePay
import io.primer.android.payment.paypal.PayPal
import io.primer.android.viewmodel.PrimerViewModel

internal abstract class PaymentMethodDescriptor(
  protected val viewModel: PrimerViewModel,
  protected val config: PaymentMethodRemoteConfig
  ): ITokenizable {

  abstract val identifier: String

  abstract val selectedBehaviour: SelectedPaymentMethodBehaviour

  abstract val type: PaymentMethodType

  abstract val vaultCapability: VaultCapability

  abstract fun createButton(container: ViewGroup): View

  class Factory(private val viewModel: PrimerViewModel) {
    fun create(config: PaymentMethodRemoteConfig, options: PaymentMethod): PaymentMethodDescriptor? {
      // TODO: hate this - think of a better way
      return when (config.type) {
        PAYMENT_CARD_IDENTIFIER -> CreditCard(viewModel, config, options as PaymentMethod.Card)
        PAYPAL_IDENTIFIER -> PayPal(viewModel, config, options as PaymentMethod.PayPal)
        GOOGLE_PAY_IDENTIFIER -> GooglePay(viewModel, config, options as PaymentMethod.GooglePay)
        else -> null
      }
    }
  }
}
