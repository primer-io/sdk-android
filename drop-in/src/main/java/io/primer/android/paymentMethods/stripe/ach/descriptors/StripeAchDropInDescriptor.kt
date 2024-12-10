package io.primer.android.paymentMethods.stripe.ach.descriptors

import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.ui.fragments.stripe.ach.StripeAchUserDetailsCollectionFragment

internal data class StripeAchDropInDescriptor(
    override val paymentMethodType: String,
    override val uiOptions: UiOptions
) : PaymentMethodDropInDescriptor {

    override val selectedBehaviour: PaymentMethodBehaviour
        get() = NewFragmentBehaviour(
            StripeAchUserDetailsCollectionFragment::newInstance,
            returnToPreviousOnBack = uiOptions.isStandalonePaymentMethod.not()
        )

    override val uiType: PaymentMethodUiType
        get() = PaymentMethodUiType.FORM

    override val behaviours: List<PaymentMethodBehaviour>
        get() = emptyList()

    override val loadingState: LoadingState?
        get() = null
}
