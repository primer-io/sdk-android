package io.primer.android.webredirect.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.payments.di.PaymentsContainer
import io.primer.android.webredirect.implementation.composer.presentation.WebRedirectComponent
import io.primer.android.webredirect.implementation.payment.presentation.delegate.presentation.WebRedirectPaymentDelegate
import io.primer.android.webredirect.implementation.tokenization.presentation.WebRedirectTokenizationDelegate

internal class WebRedirectComposerProviderFactory : PaymentMethodComposerProvider.Factory {
    override fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    ): PaymentMethodComposer {
        return WebRedirectComponent(
            tokenizationDelegate =
                WebRedirectTokenizationDelegate(
                    configurationInteractor = resolve(name = paymentMethodType),
                    tokenizationInteractor = resolve(name = paymentMethodType),
                    deeplinkInteractor = resolve(name = paymentMethodType),
                    platformResolver = resolve(name = paymentMethodType),
                ),
            pollingInteractor = resolve(PaymentsContainer.POLLING_INTERACTOR_DI_KEY),
            paymentDelegate =
                WebRedirectPaymentDelegate(
                    paymentMethodTokenHandler = resolve(),
                    resumePaymentHandler = resolve(),
                    successHandler = resolve(),
                    errorHandler = resolve(),
                    baseErrorResolver = resolve(),
                    webRedirectResumeHandler = resolve(),
                ),
        )
    }
}
