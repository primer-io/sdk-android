package io.primer.android.payment.async.promptpay

import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.payment.LoadingState

internal class RapydPromptPayPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "PROMPTPAY"

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(
            NewFragmentBehaviour(
                PaymentMethodLoadingFragment::newInstance,
                returnToPreviousOnBack = true
            )
        )

    override fun getLoadingState() = LoadingState(
        if (localConfig.settings.uiOptions.theme.isDarkMode == true)
            R.drawable.ic_logo_promptpay_dark else R.drawable.ic_logo_promptpay_light
    )
}
