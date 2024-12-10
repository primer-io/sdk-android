package io.primer.android.paypal.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.paypal.implementation.composer.presentation.PaypalComponent
import io.primer.android.paypal.implementation.payment.presentation.delegate.presentation.PaypalPaymentDelegate
import io.primer.android.paypal.implementation.tokenization.presentation.PaypalTokenizationDelegate

internal class PaypalComposerProviderFactory : PaymentMethodComposerProvider.Factory {

    override fun create(paymentMethodType: String, sessionIntent: PrimerSessionIntent): PaymentMethodComposer {
        return PaypalComponent(
            tokenizationCollectorDelegate = resolve(name = paymentMethodType),
            tokenizationDelegate = PaypalTokenizationDelegate(
                tokenizationInteractor = resolve(name = paymentMethodType),
                paypalCreateOrderInteractor = resolve(),
                confirmBillingAgreementInteractor = resolve()
            ),
            paymentDelegate = PaypalPaymentDelegate(
                paymentMethodTokenHandler = resolve(),
                resumePaymentHandler = resolve(),
                successHandler = resolve(),
                errorHandler = resolve(),
                baseErrorResolver = resolve()
            )
        )
    }
}
