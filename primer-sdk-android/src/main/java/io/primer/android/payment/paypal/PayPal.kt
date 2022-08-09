package io.primer.android.payment.paypal

import android.content.Context
import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodModule
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class PayPal(override val type: String = PaymentMethodType.PAYPAL.name) :
    PaymentMethod {

    override val canBeVaulted: Boolean = true

    override val module: PaymentMethodModule = object : PaymentMethodModule {
        override fun initialize(applicationContext: Context, configuration: ConfigurationData) {
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
}
