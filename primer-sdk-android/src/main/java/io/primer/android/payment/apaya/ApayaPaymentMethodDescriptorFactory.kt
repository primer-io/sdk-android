package io.primer.android.payment.apaya

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class ApayaPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {

    override fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodConfigDataResponse,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry
    ): PaymentMethodDescriptor =
        ApayaDescriptor(
            localConfig,
            paymentMethod as Apaya,
            paymentMethodRemoteConfig
        )
}
