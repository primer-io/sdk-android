package io.primer.android.payment.paypal

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class PayPalPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {

    override fun create(
        config: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodConfigDataResponse,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor = when (paymentMethodRemoteConfig.type) {
        PaymentMethodType.PRIMER_TEST_PAYPAL.name -> PrimerTestPayPalDescriptor(
            config, paymentMethodRemoteConfig
        )
        else -> PayPalDescriptor(config, paymentMethodRemoteConfig)
    }
}
