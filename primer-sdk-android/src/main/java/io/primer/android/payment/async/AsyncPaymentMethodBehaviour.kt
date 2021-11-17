package io.primer.android.payment.async

import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension

internal abstract class InitialTokenizationBehaviour : SelectedPaymentMethodBehaviour() {

    abstract fun execute(tokenizationViewModel: TokenizationViewModel)
}

@KoinApiExtension
internal class AsyncPaymentMethodBehaviour(private val asyncMethod: AsyncPaymentMethodDescriptor) :
    InitialTokenizationBehaviour() {
    override fun execute(tokenizationViewModel: TokenizationViewModel) {
        tokenizationViewModel.resetPaymentMethod(asyncMethod)
        asyncMethod.setTokenizableValue("type", "OFF_SESSION_PAYMENT")
        asyncMethod.setTokenizableValue("paymentMethodType", asyncMethod.config.type.name)
        asyncMethod.setTokenizableValue("paymentMethodConfigId", asyncMethod.config.id!!)
        // ...
        asyncMethod.appendTokenizableValue(
            "sessionInfo",
            "locale",
            asyncMethod.localConfig.settings.options.locale.toLanguageTag()
        )
        asyncMethod.appendTokenizableValue("sessionInfo", "platform", "ANDROID")

        tokenizationViewModel.tokenize()
    }
}
