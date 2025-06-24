package io.primer.android.internal.data.mappers

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod

internal interface PaymentMethodMapper {
    fun toComposable(
        headless: PrimerHeadlessUniversalCheckoutPaymentMethod,
        surcharges: Map<String, Surcharge> = emptyMap(),
    ): PrimerComposablePaymentMethod
}

internal class PaymentMethodMapperImpl : PaymentMethodMapper {

    override fun toComposable(
        headless: PrimerHeadlessUniversalCheckoutPaymentMethod,
        surcharges: Map<String, Surcharge>,
    ): PrimerComposablePaymentMethod {
        return PrimerComposablePaymentMethod(
            paymentMethodType = headless.paymentMethodType,
            paymentMethodName = headless.paymentMethodName,
            supportedPrimerSessionIntents = headless.supportedPrimerSessionIntents,
            paymentMethodManagerCategories = headless.paymentMethodManagerCategories,
            requiredInputDataClass = headless.requiredInputDataClass,
            surcharge = surcharges[headless.paymentMethodType],
        )
    }
}
