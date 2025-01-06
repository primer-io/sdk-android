package io.primer.android.payment

import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import io.primer.android.R
import io.primer.android.paymentMethods.PaymentMethodBehaviour

internal open class NewFragmentBehaviour(
    @get:VisibleForTesting
    val factory: (() -> Fragment),
    @get:VisibleForTesting
    val returnToPreviousOnBack: Boolean = false,
    private val tag: String? = null,
    /**
     * Whether to replace the previous fragment, effectively triggering its onDestroy() callback, or to hide it before
     * adding the new fragment to the sheet.
     */
    val replacePreviousFragment: Boolean = true,
) : PaymentMethodBehaviour {
    fun execute(parent: Fragment) {
        val fragment = factory()
        openFragment(parent, fragment, returnToPreviousOnBack)
    }

    private fun openFragment(
        parent: Fragment,
        newFragment: Fragment,
        returnToPreviousOnBack: Boolean,
    ) {
        parent.childFragmentManager
            .beginTransaction()
            .run {
                if (returnToPreviousOnBack) {
                    addToBackStack(tag)
                        .replace(R.id.checkout_sheet_content, newFragment)
                } else {
                    if (replacePreviousFragment) {
                        replace(R.id.checkout_sheet_content, newFragment)
                    } else {
                        parent.childFragmentManager.fragments.lastOrNull()?.let { hide(it) }
                        add(R.id.checkout_sheet_content, newFragment)
                    }
                }
            }
            .commit()
    }
}
