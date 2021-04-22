package io.primer.android

import android.content.Context
import io.primer.android.model.OrderItem
import io.primer.android.payment.GOCARDLESS_IDENTIFIER
import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import io.primer.android.payment.KLARNA_IDENTIFIER
import io.primer.android.payment.PAYMENT_CARD_IDENTIFIER
import io.primer.android.payment.PAYPAL_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.payment.card.CardPaymentMethodDescriptorFactory
import io.primer.android.payment.gocardless.GoCardlessPaymentMethodDescriptorFactory
import io.primer.android.payment.google.GoogleModule
import io.primer.android.payment.klarna.KlarnaPaymentMethodDescriptorFactory
import io.primer.android.payment.paypal.PayPalPaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.*

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
class Card : PaymentMethod {

    override val identifier: String = PAYMENT_CARD_IDENTIFIER

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
                PAYMENT_CARD_IDENTIFIER,
                CardPaymentMethodDescriptorFactory()
            )
        }
    }
    override val serializersModule: SerializersModule
        get() = cardSerializationModule
}

val cardSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(Card::class)
    }
}

@Serializable
class PayPal : PaymentMethod {

    override val identifier: String = PAYPAL_IDENTIFIER

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
                PAYPAL_IDENTIFIER,
                PayPalPaymentMethodDescriptorFactory()
            )
        }
    }
    override val serializersModule: SerializersModule
        get() = payPalSerializationModule
}

val payPalSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(PayPal::class)
    }
}

//
//

@Serializable
data class GooglePay(
    val merchantName: String,
    val totalPrice: String,
    val countryCode: String,
    val currencyCode: String,
    val allowedCardNetworks: List<String> = listOf(
        "AMEX",
        "DISCOVER",
        "JCB",
        "MASTERCARD",
        "VISA"
    ),
) : PaymentMethod {

    override val identifier: String = GOOGLE_PAY_IDENTIFIER

    internal val allowedCardAuthMethods: List<String> = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
    internal val billingAddressRequired: Boolean = false

    override val module: PaymentMethodModule by lazy { GoogleModule() }

    override val serializersModule: SerializersModule
        get() = googlePaySerializationModule
}

val googlePaySerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(GooglePay::class)
    }
}

//
//

@Serializable
data class Klarna(val orderItems: List<OrderItem>) : PaymentMethod {

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

//
//

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
        get() = googlePaySerializationModule
}

val goCardlessSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(GoCardless::class)
    }
}

