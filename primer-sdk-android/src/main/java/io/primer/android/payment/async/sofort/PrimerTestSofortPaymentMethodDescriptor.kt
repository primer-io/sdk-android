package io.primer.android.payment.async.sofort

import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.NewMiddleFragmentBehaviour
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.async.AsyncPaymentMethod
import io.primer.android.payment.dummy.DummyDecisionType
import io.primer.android.payment.dummy.DummyResultDescriptorHandler
import io.primer.android.ui.fragments.dummy.DummyResultSelectorFragment

internal class PrimerTestSofortPaymentMethodDescriptor(
    override val localConfig: PrimerConfig,
    override val options: AsyncPaymentMethod,
    config: PaymentMethodRemoteConfig,
) : SofortPaymentMethodDescriptor(localConfig, options, config), DummyResultDescriptorHandler {

    override val selectedBehaviour: SelectedPaymentMethodBehaviour
        get() = NewMiddleFragmentBehaviour(
            DummyResultSelectorFragment::newInstance,
            onActionContinue = { super.selectedBehaviour },
            returnToPreviousOnBack = true
        )

    override fun setDecision(decision: DummyDecisionType) {
        appendTokenizableValue(
            "sessionInfo",
            "flowDecision",
            decision.name
        )

        setTokenizableValue("type", "OFF_SESSION_PAYMENT")
        setTokenizableValue("paymentMethodType", config.type.name)
        setTokenizableValue("paymentMethodConfigId", config.id!!)
        appendTokenizableValue("sessionInfo", "platform", "ANDROID")
    }
}
