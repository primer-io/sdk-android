package io.primer.android.otp

import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactory

internal class OtpDescriptorFactory : PaymentMethodDescriptorFactory {
    override fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodConfigDataResponse,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor = OtpDescriptor(localConfig, paymentMethodRemoteConfig, paymentMethod.type)
}
