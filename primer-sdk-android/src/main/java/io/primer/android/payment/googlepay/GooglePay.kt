package io.primer.android.payment.googlepay

import android.view.View
import android.view.ViewGroup
import io.primer.android.GOOGLE_PAY_IDENTIFIER
import io.primer.android.PaymentMethod
import io.primer.android.payment.*
import io.primer.android.viewmodel.PrimerViewModel

internal class GooglePay(
  viewModel: PrimerViewModel,
  config: PaymentMethodRemoteConfig,
  private val options: PaymentMethod.GooglePay
  ): PaymentMethodDescriptor(viewModel, config) {

  override val selectedBehaviour: SelectedPaymentMethodBehaviour
    get() = NoopBehaviour()

  override val identifier: String
    get() = GOOGLE_PAY_IDENTIFIER

  override val vaultCapability: VaultCapability
    get() = VaultCapability.SINGLE_USE_ONLY

  override val type: PaymentMethodType
    get() = PaymentMethodType.SIMPLE_BUTTON

  override fun createButton(container: ViewGroup): View {
    TODO("Not yet implemented")
  }
}