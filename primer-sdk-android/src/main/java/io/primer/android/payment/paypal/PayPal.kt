package io.primer.android.payment.paypal

import android.view.View
import android.view.ViewGroup
import io.primer.android.PAYPAL_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodType
import io.primer.android.payment.VaultCapability
import io.primer.android.ui.PrimerViewModel
import org.json.JSONObject

internal class PayPal(viewModel: PrimerViewModel, config: PaymentMethodRemoteConfig):
  PaymentMethodDescriptor(viewModel, config) {
  override val identifier: String
    get() = PAYPAL_IDENTIFIER

  override val type: PaymentMethodType
    get() = PaymentMethodType.SIMPLE_BUTTON

  override val vaultCapability: VaultCapability
    get() = VaultCapability.SINGLE_USE_AND_VAULT

  override fun createButton(container: ViewGroup): View {
    TODO("Not yet implemented")
  }

  override fun toPaymentInstrument(): JSONObject {
    TODO("Not yet implemented")
  }
}