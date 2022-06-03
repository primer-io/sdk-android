package io.primer.android.payment.gocardless

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
internal data class GoCardless(
    val companyName: String? = null,
    val companyAddress: String? = null,
    val customerName: String? = null,
    val customerEmail: String? = null,
    val customerAddressLine1: String? = null,
    val customerAddressLine2: String? = null,
    val customerAddressCity: String? = null,
    val customerAddressState: String? = null,
    val customerAddressCountryCode: String? = null,
    val customerAddressPostalCode: String? = null,
) : PaymentMethod {

    override val type = PaymentMethodType.GOCARDLESS

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
                GoCardlessPaymentMethodDescriptorFactory()
            )
        }
    }
    override val serializersModule: SerializersModule
        get() = goCardlessSerializationModule
}

private val goCardlessSerializationModule: SerializersModule = SerializersModule {
    polymorphic(PaymentMethod::class) {
        subclass(GoCardless::class, GoCardless.serializer())
    }
}
