package io.primer.android.payment.klarna

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class KlarnaPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {

    override fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodConfigDataResponse,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry
    ): PaymentMethodDescriptor = when (paymentMethodRemoteConfig.type) {
        PaymentMethodType.PRIMER_TEST_KLARNA.name -> PrimerTestKlarnaDescriptor(
            paymentMethod as Klarna,
            localConfig,
            paymentMethodRemoteConfig
        )
        else -> KlarnaDescriptor(
            paymentMethod as Klarna,
            localConfig,
            paymentMethodRemoteConfig
        )
    }
}
