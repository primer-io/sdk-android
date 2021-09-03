package io.primer.android.payment.apaya

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class ApayaPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {

    override fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor =
        ApayaDescriptor(
            checkoutConfig,
            paymentMethod as Apaya,
            paymentMethodRemoteConfig
        )
}
