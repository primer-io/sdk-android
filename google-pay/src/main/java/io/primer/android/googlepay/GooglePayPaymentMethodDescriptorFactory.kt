package io.primer.android.googlepay

import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactory

internal class GooglePayPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {
    override fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodConfigDataResponse,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): GooglePayDescriptor =
        GooglePayDescriptor(
            localConfig = localConfig,
            options = paymentMethod as GooglePay,
            config = paymentMethodRemoteConfig,
        )
}
