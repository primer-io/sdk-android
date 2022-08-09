package io.primer.android.payment.async.dotpay

import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.bank.DotPayBankSelectionFragment

internal class AdyenDotpayPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val title = "DOTPAY"

    override val selectedBehaviour =
        NewFragmentBehaviour(
            DotPayBankSelectionFragment::newInstance, returnToPreviousOnBack = true
        )

    override val behaviours: List<SelectedPaymentMethodBehaviour>
        get() = listOf(AsyncPaymentMethodBehaviour(this))

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM
}
