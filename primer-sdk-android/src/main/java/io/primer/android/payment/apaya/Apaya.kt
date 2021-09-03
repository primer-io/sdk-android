package io.primer.android.payment.apaya

import android.content.Context
import androidx.annotation.Keep
import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.model.dto.ClientSession
import io.primer.android.payment.APAYA_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Keep
@Serializable
data class Apaya(val webViewTitle: String = "Pay by mobile") : PaymentMethod {

    override val identifier: String = APAYA_IDENTIFIER

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
                APAYA_IDENTIFIER,
                ApayaPaymentMethodDescriptorFactory()
            )
        }
    }

    override val serializersModule: SerializersModule
        get() = apayaSerializationModule
}

private val apayaSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(Apaya::class, Apaya.serializer())
    }
}
