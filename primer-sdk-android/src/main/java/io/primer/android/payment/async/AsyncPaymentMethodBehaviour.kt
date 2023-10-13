package io.primer.android.payment.async

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.viewmodel.TokenizationViewModel

internal abstract class InitialTokenizationBehaviour : SelectedPaymentMethodBehaviour() {

    abstract fun execute(tokenizationViewModel: TokenizationViewModel)
}

internal open class AsyncPaymentMethodBehaviour(
    private val asyncMethod: AsyncPaymentMethodDescriptor
) : InitialTokenizationBehaviour() {

    protected open val instrumentType = PaymentInstrumentType.OFF_SESSION_PAYMENT

    override fun execute(tokenizationViewModel: TokenizationViewModel) {
        tokenizationViewModel.resetPaymentMethod(asyncMethod)
        asyncMethod.setTokenizableValue("type", instrumentType.name)
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
            tokenizationViewModel.getRedirectionUrl()
        )

        onPreTokenize(asyncMethod)
        tokenizationViewModel.tokenize()
    }

    internal open fun onPreTokenize(asyncMethod: AsyncPaymentMethodDescriptor) = Unit
}

internal class CardAsyncPaymentMethodBehaviour(asyncMethod: AsyncPaymentMethodDescriptor) :
    AsyncPaymentMethodBehaviour(asyncMethod) {

    override val instrumentType = PaymentInstrumentType.CARD_OFF_SESSION_PAYMENT

    override fun onPreTokenize(asyncMethod: AsyncPaymentMethodDescriptor) {
        asyncMethod.appendTokenizableValue(
            "sessionInfo",
            "browserInfo",
            "userAgent",
            System.getProperty("http.agent").orEmpty()
        )
    }
}
