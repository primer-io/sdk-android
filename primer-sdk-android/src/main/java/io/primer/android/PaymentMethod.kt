package io.primer.android

import android.content.Context
import io.primer.android.model.OrderItem
import io.primer.android.payment.GOCARDLESS_IDENTIFIER
import io.primer.android.payment.KLARNA_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.payment.gocardless.GoCardlessPaymentMethodDescriptorFactory
import io.primer.android.payment.klarna.KlarnaPaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.modules.SerializersModule

// this new way of declaring PaymentMethods is meant to improve modularisation as each method can
// be declared in a separate module (provided the marker interface is declared in shared one)
interface PaymentMethod {

    // this is here for backwards compatibility with old_PaymentMethod (to avoid a larger refactor)
    val identifier: String

    @Transient
    val module: PaymentMethodModule

    @Transient
    val serializersModule: SerializersModule
}

@Serializable
data class Klarna(
    val orderDescription: String,
    val orderItems: List<OrderItem> = emptyList(),
) : PaymentMethod {

    override val identifier: String = KLARNA_IDENTIFIER

    @Transient
    override val module: PaymentMethodModule = object : PaymentMethodModule {
        override fun initialize(applicationContext: Context) {
            // TODO: initialize not implemented
        }

        override fun registerPaymentMethodCheckers(
            paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
        ) {
            // TODO: registerPaymentMethodCheckers not implemented
        }

        override fun registerPaymentMethodDescriptorFactory(
            paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
        ) {
            paymentMethodDescriptorFactoryRegistry.register(
                KLARNA_IDENTIFIER,
                KlarnaPaymentMethodDescriptorFactory()
            )
        }
    }
    override val serializersModule: SerializersModule
        get() = klarnaSerializationModule
}

val klarnaSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(Klarna::class)
    }
}

@Serializable
data class GoCardless(
    val companyName: String,
    val companyAddress: String,
    val customerName: String? = null,
    val customerEmail: String? = null,
    val customerAddressLine1: String? = null,
    val customerAddressLine2: String? = null,
    val customerAddressCity: String? = null,
    val customerAddressState: String? = null,
    val customerAddressCountryCode: String? = null,
    val customerAddressPostalCode: String? = null,
) : PaymentMethod {

    override val identifier: String = GOCARDLESS_IDENTIFIER

    @Transient
    override val module: PaymentMethodModule = object : PaymentMethodModule {
        override fun initialize(applicationContext: Context) {
            // TODO: initialize not implemented
        }

        override fun registerPaymentMethodCheckers(
            paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
        ) {
            // TODO: registerPaymentMethodCheckers not implemented
        }

        override fun registerPaymentMethodDescriptorFactory(
            paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
        ) {
            paymentMethodDescriptorFactoryRegistry.register(
                GOCARDLESS_IDENTIFIER,
                GoCardlessPaymentMethodDescriptorFactory()
            )
        }
    }
    override val serializersModule: SerializersModule
        get() = goCardlessSerializationModule
}

val goCardlessSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(GoCardless::class)
    }
}
