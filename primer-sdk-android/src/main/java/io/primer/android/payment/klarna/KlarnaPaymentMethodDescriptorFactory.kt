package io.primer.android.payment.klarna

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.SinglePaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class KlarnaPaymentMethodDescriptorFactory : SinglePaymentMethodDescriptorFactory {

    override fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor =
        Klarna(
            checkoutConfig,
            paymentMethod as PaymentMethod.Klarna,
            paymentMethodRemoteConfig
        )
}
