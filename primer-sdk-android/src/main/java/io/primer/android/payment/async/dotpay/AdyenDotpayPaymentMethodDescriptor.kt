package io.primer.android.payment.async.dotpay

import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.bank.DotPayBankSelectionFragment

internal class AdyenDotpayPaymentMethodDescriptor(
    override val options: AsyncPaymentMethod,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse
) : AsyncPaymentMethodDescriptor(options, localConfig, config) {

    override val selectedBehaviour =
        NewFragmentBehaviour(
            DotPayBankSelectionFragment::newInstance,
            returnToPreviousOnBack = localConfig.isStandalonePaymentMethod.not()
        )

    override val behaviours: List<SelectedPaymentMethodBehaviour>
        get() = listOf(AsyncPaymentMethodBehaviour(this))

    override val sdkCapabilities: List<SDKCapability>
        get() = listOf(SDKCapability.DROP_IN)

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val headlessDefinition: HeadlessDefinition?
        get() = null
}
