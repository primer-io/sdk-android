package io.primer.android.payment.async

import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.viewmodel.TokenizationViewModel

internal abstract class InitialTokenizationBehaviour : SelectedPaymentMethodBehaviour() {

    abstract fun execute(tokenizationViewModel: TokenizationViewModel)
}

internal class AsyncPaymentMethodBehaviour(private val asyncMethod: AsyncPaymentMethodDescriptor) :
    InitialTokenizationBehaviour() {
    override fun execute(tokenizationViewModel: TokenizationViewModel) {
        tokenizationViewModel.resetPaymentMethod(asyncMethod)
        asyncMethod.setTokenizableValue("type", "OFF_SESSION_PAYMENT")
        asyncMethod.setTokenizableValue("paymentMethodType", asyncMethod.config.type)
        asyncMethod.setTokenizableValue("paymentMethodConfigId", asyncMethod.config.id!!)
        // ...
        asyncMethod.appendTokenizableValue(
            "sessionInfo",
            "locale",
            asyncMethod.localConfig.settings.locale.toLanguageTag()
        )
        asyncMethod.appendTokenizableValue("sessionInfo", "platform", "ANDROID")
        asyncMethod.appendTokenizableValue(
            "sessionInfo",
            "redirectionUrl",
            tokenizationViewModel.asyncRedirectUrl.value.orEmpty()
        )

        tokenizationViewModel.tokenize()
    }
}
