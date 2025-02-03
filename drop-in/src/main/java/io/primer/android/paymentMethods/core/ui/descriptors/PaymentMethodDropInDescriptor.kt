@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.core.ui.descriptors

import io.primer.android.payment.NativeUiPaymentMethodManagerCancellationBehaviour
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.SessionCompleteFragment
import io.primer.android.ui.fragments.SessionCompleteViewType
import io.primer.android.viewmodel.ViewStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal data class UiOptions(
    val isStandalonePaymentMethod: Boolean,
    val isInitScreenEnabled: Boolean,
    val isDarkMode: Boolean?,
)

internal interface PaymentMethodDropInDescriptor {
    val paymentMethodType: String

    val uiOptions: UiOptions

    val selectedBehaviour: PaymentMethodBehaviour

    val uiType: PaymentMethodUiType

    fun createSuccessBehavior(viewStatus: ViewStatus.ShowSuccess): NewFragmentBehaviour =
        NewFragmentBehaviour(
            factory = {
                SessionCompleteFragment.newInstance(
                    delay = viewStatus.delay,
                    viewType = SessionCompleteViewType.Success(viewStatus.successType),
                )
            },
            returnToPreviousOnBack = false,
        )

    fun createPollingStartedBehavior(viewStatus: ViewStatus.PollingStarted): NewFragmentBehaviour? = null

    /**
     The logic is the following:
     1. if we are launched using `showPaymentMethod` (isStandalonePaymentMethod = true) and
     we have disabled initial screen (isInitScreenEnabled.not()), we won't show loading screen.
     2. Otherwise, we show loading screen.
     */
    val behaviours: List<PaymentMethodBehaviour>
        get() =
            if (uiOptions.isInitScreenEnabled.not() &&
                uiOptions.isStandalonePaymentMethod
            ) {
                listOf()
            } else {
                listOf(NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance))
            }

    val cancelBehaviour: NativeUiPaymentMethodManagerCancellationBehaviour?
        get() = null

    val loadingState: LoadingState?
}
