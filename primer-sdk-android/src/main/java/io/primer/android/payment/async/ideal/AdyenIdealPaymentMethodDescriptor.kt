package io.primer.android.payment.async.ideal

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.async.AsyncPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.fragments.bank.IdealBankSelectionFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class AdyenIdealPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodConfigDataResponse,
) : AsyncPaymentMethodDescriptor(localConfig, options, config) {

    override val selectedBehaviour =
        NewFragmentBehaviour(IdealBankSelectionFragment::newInstance, returnToPreviousOnBack = true)

    override val behaviours: List<SelectedPaymentMethodBehaviour> =
        listOf(AsyncPaymentMethodBehaviour(this))

    override val sdkCapabilities: List<SDKCapability>
        get() = listOf(SDKCapability.DROP_IN)

    override val type: PaymentMethodUiType = PaymentMethodUiType.FORM

    override val headlessDefinition: HeadlessDefinition
        get() = HeadlessDefinition(listOf(PrimerPaymentMethodManagerCategory.RAW_DATA))
}
