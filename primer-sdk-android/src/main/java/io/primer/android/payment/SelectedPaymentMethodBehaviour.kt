package io.primer.android.payment

import androidx.fragment.app.Fragment
import io.primer.android.R

internal abstract class SelectedPaymentMethodBehaviour

internal class NewFragmentBehaviour(private val factory: (() -> Fragment)): SelectedPaymentMethodBehaviour() {
  fun execute(parent: Fragment) {
    val fragment = factory()

    parent.childFragmentManager.beginTransaction()
      .setCustomAnimations(R.anim.default_transition_in, R.anim.default_transition_out)
      .replace(R.id.checkout_sheet_content, fragment)
      .commit()
  }
}

internal class NoopBehaviour : SelectedPaymentMethodBehaviour()