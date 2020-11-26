package io.primer.android.payment

import android.view.View
import android.view.ViewGroup
import io.primer.android.ui.PrimerViewModel

abstract class PaymentMethod(viewModel: PrimerViewModel): ITokenizable {
  protected val viewModel: PrimerViewModel = viewModel

  protected val config: PaymentMethodRemoteConfig?
    get() {
      return viewModel.paymentMethods.value?.find {
        it.type == id
      }
    }

  val isConfigured: Boolean
    get() = config != null

  abstract val id: String

  abstract val isVaultable: Boolean;

  abstract fun renderPreview(container: ViewGroup)
}
