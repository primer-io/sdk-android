package io.primer.android.ipay88.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.ipay88.implementation.composer.presentation.IPay88Component
import io.primer.android.ipay88.implementation.payment.presentation.delegate.presentation.IPay88PaymentDelegate
import io.primer.android.ipay88.implementation.tokenization.presentation.IPay88TokenizationDelegate
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.payments.di.PaymentsContainer

internal class IPay88ComposerProviderFactory : PaymentMethodComposerProvider.Factory {
    override fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    ): PaymentMethodComposer {
        return IPay88Component(
            tokenizationDelegate =
            IPay88TokenizationDelegate(
                configurationInteractor = resolve(name = paymentMethodType),
                tokenizationInteractor = resolve(name = paymentMethodType),
            ),
            pollingInteractor = resolve(PaymentsContainer.POLLING_INTERACTOR_DI_KEY),
            paymentDelegate =
            IPay88PaymentDelegate(
                paymentMethodTokenHandler = resolve(),
                resumePaymentHandler = resolve(),
                successHandler = resolve(),
                errorHandler = resolve(),
                baseErrorResolver = resolve(),
                resumeHandler = resolve(),
            ),
            mockConfigurationDelegate = resolve(),
        )
    }
}
