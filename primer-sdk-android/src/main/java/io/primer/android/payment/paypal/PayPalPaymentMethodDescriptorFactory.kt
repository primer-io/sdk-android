package io.primer.android.payment.paypal

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class PayPalPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {

    override fun create(
        config: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor = when (paymentMethodRemoteConfig.type) {
        PaymentMethodType.PRIMER_TEST_PAYPAL -> PrimerTestPayPalDescriptor(
            paymentMethodRemoteConfig
        )
        else -> PayPalDescriptor(paymentMethodRemoteConfig)
    }
}
