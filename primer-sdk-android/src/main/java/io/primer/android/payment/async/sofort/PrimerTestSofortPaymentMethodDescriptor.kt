package io.primer.android.payment.async.sofort

import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.NewMiddleFragmentBehaviour
import io.primer.android.payment.HeadlessDefinition
import io.primer.android.payment.SDKCapability
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.dummy.DummyDecisionType
import io.primer.android.payment.dummy.DummyResultDescriptorHandler
import io.primer.android.ui.fragments.dummy.DummyResultSelectorFragment

internal class PrimerTestSofortPaymentMethodDescriptor(
    override val options: AsyncPaymentMethod,
    localConfig: PrimerConfig,
    config: PaymentMethodConfigDataResponse,
) : SofortPaymentMethodDescriptor(options, localConfig, config), DummyResultDescriptorHandler {

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewMiddleFragmentBehaviour(
            DummyResultSelectorFragment::newInstance,
            onActionContinue = { super.selectedBehaviour },
            returnToPreviousOnBack = localConfig.isStandalonePaymentMethod.not()
        )

    override fun setDecision(decision: DummyDecisionType) {
        appendTokenizableValue(
            "sessionInfo",
            "flowDecision",
            decision.name
        )

        setTokenizableValue("type", "OFF_SESSION_PAYMENT")
        setTokenizableValue("paymentMethodType", config.type)
        setTokenizableValue("paymentMethodConfigId", config.id!!)
        appendTokenizableValue("sessionInfo", "platform", "ANDROID")
    }

    override val sdkCapabilities: List<SDKCapability>
        get() = listOf(SDKCapability.DROP_IN)

    override val headlessDefinition: HeadlessDefinition?
        get() = null
}
