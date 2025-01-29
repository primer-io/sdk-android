@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.paymentMethods.otp

import io.primer.android.R
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.LoadingState
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.forms.DynamicFormFragment
import io.primer.android.viewmodel.ViewStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class OtpDropInDescriptor(
    override val uiOptions: UiOptions,
    override val paymentMethodType: String,
) : PaymentMethodDropInDescriptor {
    override val selectedBehaviour: PaymentMethodBehaviour
        get() =
            NewFragmentBehaviour(
                factory =
                when (paymentMethodType) {
                    PaymentMethodType.ADYEN_BLIK.name -> DynamicFormFragment::newInstance
                    else -> error("Unsupported payment method type '$paymentMethodType'")
                },
                returnToPreviousOnBack = uiOptions.isStandalonePaymentMethod.not(),
            )

    override val behaviours: List<PaymentMethodBehaviour> = emptyList()

    override fun createPollingStartedBehavior(viewStatus: ViewStatus.PollingStarted): NewFragmentBehaviour =
        NewFragmentBehaviour(
            factory = { PaymentMethodLoadingFragment.newInstance(popBackStackToRoot = true) },
            replacePreviousFragment = false,
        )

    override val loadingState =
        when (paymentMethodType) {
            PaymentMethodType.ADYEN_BLIK.name ->
                LoadingState(
                    imageResIs = R.drawable.ic_logo_blik_square,
                    textResId = R.string.payment_method_blik_loading_placeholder,
                )

            else -> error("Unsupported payment method type '$paymentMethodType'")
        }

    override val uiType: PaymentMethodUiType
        get() = PaymentMethodUiType.FORM
}
