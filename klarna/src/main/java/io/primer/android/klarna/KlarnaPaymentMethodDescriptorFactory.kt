package io.primer.android.klarna

import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodCheckerRegistry
import io.primer.android.paymentmethods.PaymentMethodDescriptor
import io.primer.android.paymentmethods.PaymentMethodDescriptorFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class KlarnaPaymentMethodDescriptorFactory : PaymentMethodDescriptorFactory {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodConfigDataResponse,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): PaymentMethodDescriptor =
        KlarnaDescriptor(
            paymentMethod as Klarna,
            localConfig,
            paymentMethodRemoteConfig,
        )
}
