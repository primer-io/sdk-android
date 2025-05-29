package io.primer.components.clean.internal.data.mappers

import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.components.clean.internal.domain.models.PaymentMethod
import io.primer.components.clean.model.PrimerPaymentMethod

internal interface PaymentMethodMapper {
    fun toInternal(public: PrimerPaymentMethod): PaymentMethod
    fun toPublic(internal: PaymentMethod): PrimerPaymentMethod
}

internal class PaymentMethodMapperImpl : PaymentMethodMapper {

    override fun toInternal(public: PrimerPaymentMethod): PaymentMethod {
        return PaymentMethod(id = "1", name = public.name, type = PaymentMethodType.PAYMENT_CARD)
    }

    override fun toPublic(internal: PaymentMethod): PrimerPaymentMethod {
        return PrimerPaymentMethod(internal.name)
    }
}
