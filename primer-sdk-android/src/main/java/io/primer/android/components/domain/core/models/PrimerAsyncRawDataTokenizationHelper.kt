package io.primer.android.components.domain.core.models

import io.primer.android.payment.async.AsyncPaymentMethodDescriptor

internal class PrimerAsyncRawDataTokenizationHelper(private val redirectionUrl: String) {

    fun setTokenizableData(descriptor: AsyncPaymentMethodDescriptor) = descriptor.apply {
        setTokenizableValue("type", "OFF_SESSION_PAYMENT")
        setTokenizableValue("paymentMethodType", config.type)
        setTokenizableValue("paymentMethodConfigId", config.id!!)
        // ...
        appendTokenizableValue(
            "sessionInfo",
            "locale",
            localConfig.settings.locale.toLanguageTag()
        )
        appendTokenizableValue("sessionInfo", "platform", "ANDROID")
        appendTokenizableValue(
            "sessionInfo",
            "redirectionUrl",
            redirectionUrl
        )
    }
}
