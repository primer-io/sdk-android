package io.primer.android.bancontact.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.bancontact.implementation.composer.AdyenBancontactComponent
import io.primer.android.bancontact.implementation.payment.delegate.AdyenBancontactPaymentDelegate
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate
import io.primer.android.payments.di.PaymentsContainer

internal class AdyenBancontactComposerProviderFactory : PaymentMethodComposerProvider.Factory {

    override fun create(paymentMethodType: String, sessionIntent: PrimerSessionIntent): PaymentMethodComposer {
        return AdyenBancontactComponent(
            tokenizationDelegate = resolve(),
            pollingInteractor = resolve(PaymentsContainer.POLLING_INTERACTOR_DI_KEY),
            paymentDelegate = resolve<PaymentMethodPaymentDelegate>(
                name = paymentMethodType
            ) as AdyenBancontactPaymentDelegate,
            cardInputDataValidator = resolve(name = paymentMethodType),
            metadataRetriever = resolve(),
            sdkAnalyticsEventLoggingDelegate = resolve(name = paymentMethodType)
        )
    }
}
