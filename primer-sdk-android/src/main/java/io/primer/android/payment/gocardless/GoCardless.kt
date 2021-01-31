package io.primer.android.payment.gocardless

import android.content.Context
import android.view.View
import io.primer.android.GOCARDLESS_IDENTIFIER
import io.primer.android.PaymentMethod
import io.primer.android.R
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.*
import io.primer.android.ui.fragments.FormFragment
import io.primer.android.viewmodel.PrimerViewModel

internal class GoCardless(
  viewModel: PrimerViewModel,
  config: PaymentMethodRemoteConfig,
  val options: PaymentMethod.GoCardless,
) : PaymentMethodDescriptor(viewModel, config) {

  override val identifier: String
    get() = GOCARDLESS_IDENTIFIER

  override val selectedBehaviour: SelectedPaymentMethodBehaviour
    get() = NewFragmentBehaviour(GoCardlessViewFragment::newInstance, returnToPreviousOnBack = true)

  override val type: PaymentMethodType
    get() = PaymentMethodType.FORM

  override val vaultCapability: VaultCapability
    get() = VaultCapability.VAULT_ONLY

  override fun createButton(context: Context): View {
    return View.inflate(context, R.layout.payment_method_button_direct_debit, null)
  }
}