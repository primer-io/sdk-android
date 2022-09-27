package io.primer.android.components.domain.core.models

import io.primer.android.data.configuration.models.PaymentInstrumentType
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor

internal open class PrimerAsyncRawDataTokenizationHelper(private val redirectionUrl: String) {

    open val paymentInstrumentType = PaymentInstrumentType.OFF_SESSION_PAYMENT

    fun setTokenizableData(descriptor: AsyncPaymentMethodDescriptor) = descriptor.apply {
        setTokenizableValue("type", paymentInstrumentType.name)
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
