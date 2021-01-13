package io.primer.android.payment.paypal

import android.content.Context
import android.view.View
import io.primer.android.PAYPAL_IDENTIFIER
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.UniversalCheckout
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.VaultCapability
import io.primer.android.viewmodel.PrimerViewModel

internal class PayPal(
  viewModel: PrimerViewModel,
  config: PaymentMethodRemoteConfig,
  private val options: PaymentMethod.PayPal
) : PaymentMethodDescriptor(viewModel, config) {
  override val identifier: String
    get() = PAYPAL_IDENTIFIER

  override val selectedBehaviour: SelectedPaymentMethodBehaviour
    get() = if (viewModel.uxMode.value == UniversalCheckout.UXMode.ADD_PAYMENT_METHOD) {
      PayPalBillingAgreementBehaviour(this, viewModel)
    } else {
      PayPalOrderBehaviour(this)
    }

  override val type: PaymentMethodType
    get() = PaymentMethodType.SIMPLE_BUTTON

  override val vaultCapability: VaultCapability
    get() = VaultCapability.SINGLE_USE_AND_VAULT

  override fun createButton(context: Context): View {
    return View.inflate(context, R.layout.payment_method_button_paypal, null)
  }
}