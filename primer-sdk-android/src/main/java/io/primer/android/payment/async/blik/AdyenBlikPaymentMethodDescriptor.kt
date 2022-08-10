package io.primer.android.payment.async.blik

import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.forms.DynamicFormFragment
import io.primer.android.ui.payment.LoadingState

internal class AdyenBlikPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "BLIK"

    override val selectedBehaviour =
        NewFragmentBehaviour(
            DynamicFormFragment::newInstance, returnToPreviousOnBack = true
        )

    override fun getLoadingState() = LoadingState(
        R.drawable.ic_logo_blik_square,
        R.string.payment_method_blik_loading_placeholder
    )

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(AsyncPaymentMethodBehaviour(this))
}
