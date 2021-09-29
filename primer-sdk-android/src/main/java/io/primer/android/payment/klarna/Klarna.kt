package io.primer.android.payment.klarna

import android.content.Context
import androidx.annotation.Keep
import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.model.OrderItem
import io.primer.android.model.dto.ClientSession
import io.primer.android.payment.KLARNA_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Keep
@Serializable
data class Klarna(
    val orderDescription: String? = null,
    val orderItems: List<OrderItem> = emptyList(),
    val webViewTitle: String? = "Klarna",
) : PaymentMethod {

    override val identifier: String = KLARNA_IDENTIFIER

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
                KLARNA_IDENTIFIER,
                KlarnaPaymentMethodDescriptorFactory()
            )
        }
    }
    override val serializersModule: SerializersModule
        get() = klarnaSerializationModule
}

private val klarnaSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(Klarna::class, Klarna.serializer())
    }
}
