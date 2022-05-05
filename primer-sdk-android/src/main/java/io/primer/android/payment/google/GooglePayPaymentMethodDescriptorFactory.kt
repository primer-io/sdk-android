package io.primer.android.payment.google

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.model.PaymentMethodRemoteConfig
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class GooglePayPaymentMethodDescriptorFactory(
    private val googlePayFacade: GooglePayFacade,
) : PaymentMethodDescriptorFactory {

    override fun create(
        localConfig: PrimerConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): GooglePayDescriptor =
        GooglePayDescriptor(
            localConfig = localConfig,
            options = paymentMethod as GooglePay,
            paymentMethodChecker = paymentMethodCheckers[paymentMethod.type]
                ?: throw Error("Missing payment method checker"),
            googlePayFacade = googlePayFacade,
            config = paymentMethodRemoteConfig
        )
}
