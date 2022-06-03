package io.primer.android.payment.paypal

import android.content.Context
import androidx.annotation.Keep
import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.data.configuration.models.Configuration
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Keep
@Serializable
internal class PayPal(override val type: PaymentMethodType = PaymentMethodType.PAYPAL) :
    PaymentMethod {

    override val canBeVaulted: Boolean = true

    @Transient
    override val module: PaymentMethodModule = object : PaymentMethodModule {
        override fun initialize(applicationContext: Context, configuration: Configuration) {
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
                type,
                PayPalPaymentMethodDescriptorFactory()
            )
        }
    }
    override val serializersModule: SerializersModule
        get() = payPalSerializationModule
}

private val payPalSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(PayPal::class, PayPal.serializer())
    }
}
