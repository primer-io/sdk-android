package io.primer.components.ui.components

import io.primer.components.Primer
import io.primer.components.models.PaymentMethod
import io.primer.components.models.PaymentTypeFactory
import io.primer.components.ui.components.cardform.CardFormFactory
import io.primer.components.ui.components.native.NativeFactory
import io.primer.components.ui.components.redirect.RedirectFactory

object PaymentMethodFactoryProvider {

    private val factories: Map<PaymentMethod.Type, PaymentTypeFactory<out Primer.Scope.PaymentMethod>> = mapOf(
        PaymentMethod.Type.CARD to CardFormFactory(),
        PaymentMethod.Type.GOOGLE_PAY to NativeFactory(),
        PaymentMethod.Type.KLARNA to RedirectFactory()
    )

    fun <T : Primer.Scope.PaymentMethod> getFactory(type: PaymentMethod.Type): PaymentTypeFactory<T> {
        @Suppress("UNCHECKED_CAST")
        return factories[type] as? PaymentTypeFactory<T>
            ?: throw IllegalArgumentException("Unsupported payment method: $type")
    }
}
