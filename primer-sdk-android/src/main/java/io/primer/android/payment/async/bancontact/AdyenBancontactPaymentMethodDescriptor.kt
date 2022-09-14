package io.primer.android.payment.async.bancontact

import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.NewMiddleFragmentBehaviour
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.PaymentMethodLoadingFragment
import io.primer.android.ui.fragments.multibanko.MultibancoConditionsFragment
import io.primer.android.ui.payment.LoadingState

internal class AdyenBancontactPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "BANCONTACT"

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = if (localConfig.settings.fromHUC) super.selectedBehaviour
        else NewMiddleFragmentBehaviour(
            MultibancoConditionsFragment::newInstance,
            onActionContinue = { super.selectedBehaviour },
            returnToPreviousOnBack = true
        )

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(NewFragmentBehaviour(PaymentMethodLoadingFragment::newInstance, true))

    override fun getLoadingState() = LoadingState(
        if (localConfig.settings.uiOptions.theme.isDarkMode == true)
            R.drawable.ic_logo_bancontact_dark else R.drawable.ic_logo_bancontact
    )
}
