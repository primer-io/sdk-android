package io.primer.android.payment.async

import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.viewmodel.TokenizationViewModel

internal abstract class InitialTokenizationBehaviour : SelectedPaymentMethodBehaviour() {

    abstract fun execute(tokenizationViewModel: TokenizationViewModel)
}

internal open class AsyncPaymentMethodBehaviour(
    private val asyncMethod: AsyncPaymentMethodDescriptor
) : InitialTokenizationBehaviour() {

    protected open val instrumentType = "OFF_SESSION_PAYMENT"

    override fun execute(tokenizationViewModel: TokenizationViewModel) {
        tokenizationViewModel.resetPaymentMethod(asyncMethod)
        asyncMethod.setTokenizableValue("type", instrumentType)
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

internal class CardAsyncPaymentMethodBehaviour(asyncMethod: AsyncPaymentMethodDescriptor) :
    AsyncPaymentMethodBehaviour(asyncMethod) {

    override val instrumentType: String = "CARD_OFF_SESSION_PAYMENT"
}
