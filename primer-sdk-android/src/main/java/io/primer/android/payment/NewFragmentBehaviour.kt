package io.primer.android.payment

import androidx.fragment.app.Fragment
import io.primer.android.R

internal class NewFragmentBehaviour(
    private val factory: (() -> Fragment),
    private val returnToPreviousOnBack: Boolean = false,
) : SelectedPaymentMethodBehaviour() {

    fun execute(parent: Fragment) {
        val fragment = factory()
        openFragment(parent, fragment, returnToPreviousOnBack)
    }

    private fun openFragment(
        parent: Fragment,
        newFragment: Fragment,
        returnToPreviousOnBack: Boolean
    ) {
        parent.childFragmentManager
            .beginTransaction()
            .apply {
                if (returnToPreviousOnBack) {
                    addToBackStack(null)
                }
            }
            .replace(R.id.checkout_sheet_content, newFragment)
            .commit()
    }
}
