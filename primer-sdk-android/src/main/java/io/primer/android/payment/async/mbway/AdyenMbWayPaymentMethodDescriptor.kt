package io.primer.android.payment.async.mbway

import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.forms.DynamicFormFragment
import io.primer.android.ui.payment.LoadingState

internal class AdyenMbWayPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "MB WAY"

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override fun getLoadingState() =
        LoadingState(
            when (localConfig.settings.uiOptions.theme.isDarkMode) {
                true -> R.drawable.ic_logo_mbway_dark
                else -> R.drawable.ic_logo_mbway_light
            },
            R.string.completeYourPaymentInTheApp,
            title
        )

    override val selectedBehaviour =
        NewFragmentBehaviour(
            DynamicFormFragment::newInstance, returnToPreviousOnBack = true
        )

    override val behaviours: List<SelectedPaymentMethodBehaviour>
        get() = listOf(
            AsyncPaymentMethodBehaviour(this),
            NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance)
        )
}
