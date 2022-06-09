package io.primer.android.payment.paypal

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodRemoteConfig
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class PayPalPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {

    override fun create(
        config: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor =
        PayPalDescriptor(
            paymentMethodRemoteConfig,
            paymentMethod as PayPal
        )
}
