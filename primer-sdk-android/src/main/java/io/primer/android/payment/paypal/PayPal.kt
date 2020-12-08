package io.primer.android.payment.paypal

import android.view.View
import android.view.ViewGroup
import io.primer.android.PAYPAL_IDENTIFIER
import io.primer.android.PaymentMethod
import io.primer.android.payment.*
import io.primer.android.viewmodel.PrimerViewModel

internal class PayPal(
  viewModel: PrimerViewModel,
  config: PaymentMethodRemoteConfig,
  private val options: PaymentMethod.PayPal
): PaymentMethodDescriptor(viewModel, config) {
  override val identifier: String
    get() = PAYPAL_IDENTIFIER

  override val selectedBehaviour: SelectedPaymentMethodBehaviour
    get() = NoopBehaviour()

  override val type: PaymentMethodType
    get() = PaymentMethodType.SIMPLE_BUTTON

  override val vaultCapability: VaultCapability
    get() = VaultCapability.SINGLE_USE_AND_VAULT

  override fun createButton(container: ViewGroup): View {
    TODO("Not yet implemented")
  }
}