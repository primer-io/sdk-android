package io.primer.android.payment

import androidx.fragment.app.Fragment
import io.primer.android.R

internal abstract class SelectedPaymentMethodBehaviour

internal class NewFragmentBehaviour(
  private val factory: (() -> Fragment),
  private val returnToPreviousOnBack: Boolean = false
): SelectedPaymentMethodBehaviour() {
  fun execute(parent: Fragment) {
    val fragment = factory()
    val transaction = parent.childFragmentManager.beginTransaction()

    if (returnToPreviousOnBack) {
      transaction.addToBackStack(null)
    }

    transaction.setCustomAnimations(R.anim.default_transition_in, R.anim.default_transition_out)
    transaction.replace(R.id.checkout_sheet_content, fragment)
    transaction.commit()
  }
}

internal class NoopBehaviour : SelectedPaymentMethodBehaviour()