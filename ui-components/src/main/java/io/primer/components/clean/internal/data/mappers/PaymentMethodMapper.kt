package io.primer.components.clean.internal.data.mappers

import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType.Companion.safeValueOf
import io.primer.components.clean.internal.domain.models.PaymentMethod
import io.primer.components.clean.model.PrimerPaymentMethod

internal interface PaymentMethodMapper {
    fun toInternal(public: PrimerPaymentMethod): PaymentMethod
    fun toPublic(internal: PaymentMethod): PrimerPaymentMethod
    fun toInternal(headless: PrimerHeadlessUniversalCheckoutPaymentMethod): PaymentMethod
}

internal class PaymentMethodMapperImpl : PaymentMethodMapper {

    override fun toInternal(public: PrimerPaymentMethod): PaymentMethod {
        return PaymentMethod(name = public.name, type = PaymentMethodType.PAYMENT_CARD)
    }

    override fun toPublic(internal: PaymentMethod): PrimerPaymentMethod {
        return PrimerPaymentMethod(internal.name)
    }

    override fun toInternal(headless: PrimerHeadlessUniversalCheckoutPaymentMethod): PaymentMethod {
        return PaymentMethod(name = headless.paymentMethodName!!, type = safeValueOf(headless.paymentMethodType))
    }
}
