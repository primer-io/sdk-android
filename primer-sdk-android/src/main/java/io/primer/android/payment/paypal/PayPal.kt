package io.primer.android.payment.paypal

import android.content.Context
import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.payment.PAYPAL_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
class PayPal : PaymentMethod {

    override val identifier: String = PAYPAL_IDENTIFIER

    @Transient
    override val module: PaymentMethodModule = object : PaymentMethodModule {
        override fun initialize(applicationContext: Context) {
            // no-op
        }

        override fun registerPaymentMethodCheckers(
            paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
        ) {
            // no-op
        }

        override fun registerPaymentMethodDescriptorFactory(
            paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
        ) {
            paymentMethodDescriptorFactoryRegistry.register(
                PAYPAL_IDENTIFIER,
                PayPalPaymentMethodDescriptorFactory()
            )
        }
    }
    override val serializersModule: SerializersModule
        get() = payPalSerializationModule
}

private val payPalSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(PayPal::class)
    }
}
