package io.primer.composable.internal.data.mappers

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.composable.model.PrimerComposablePaymentMethod

internal interface PaymentMethodMapper {
    fun toComposable(headless: PrimerHeadlessUniversalCheckoutPaymentMethod): PrimerComposablePaymentMethod
}

internal class PaymentMethodMapperImpl : PaymentMethodMapper {

    override fun toComposable(headless: PrimerHeadlessUniversalCheckoutPaymentMethod): PrimerComposablePaymentMethod {
        return PrimerComposablePaymentMethod(
            paymentMethodType = headless.paymentMethodType,
            paymentMethodName = headless.paymentMethodName,
            supportedPrimerSessionIntents = headless.supportedPrimerSessionIntents,
            paymentMethodManagerCategories = headless.paymentMethodManagerCategories,
            requiredInputDataClass = headless.requiredInputDataClass,
        )
    }
}