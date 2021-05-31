package io.primer.android.payment.card

import android.content.Context
import androidx.annotation.Keep
import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.model.dto.ClientSession
import io.primer.android.payment.PAYMENT_CARD_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Keep
@Serializable
class Card : PaymentMethod {

    override val identifier: String = PAYMENT_CARD_IDENTIFIER

    override val canBeVaulted: Boolean = true

    @Transient
    override val module: PaymentMethodModule = object : PaymentMethodModule {
        override fun initialize(applicationContext: Context, clientSession: ClientSession) {
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
                PAYMENT_CARD_IDENTIFIER,
                CardPaymentMethodDescriptorFactory()
            )
        }
    }
    override val serializersModule: SerializersModule
        get() = cardSerializationModule
}

private val cardSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(Card::class, Card.serializer())
    }
}
