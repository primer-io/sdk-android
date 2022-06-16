package io.primer.android.payment

import androidx.fragment.app.Fragment

internal class NewMiddleFragmentBehaviour(
    factory: () -> Fragment,
    returnToPreviousOnBack: Boolean = false,
    private val onActionContinue: () -> SelectedPaymentMethodBehaviour?
) : NewFragmentBehaviour(factory, returnToPreviousOnBack) {

    override fun handleActionContinue(): SelectedPaymentMethodBehaviour? {
        return onActionContinue()
    }
}

internal interface OnActionContinueCallback {

    fun onProvideActionContinue(onAction: () -> SelectedPaymentMethodBehaviour?)
}
