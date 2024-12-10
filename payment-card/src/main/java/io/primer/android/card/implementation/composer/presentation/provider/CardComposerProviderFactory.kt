package io.primer.android.card.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.card.implementation.composer.CardComponent
import io.primer.android.card.implementation.payment.delegate.CardPaymentDelegate
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate

internal class CardComposerProviderFactory : PaymentMethodComposerProvider.Factory {

    override fun create(paymentMethodType: String, sessionIntent: PrimerSessionIntent): PaymentMethodComposer {
        return CardComponent(
            tokenizationDelegate = resolve(),
            paymentDelegate = resolve<PaymentMethodPaymentDelegate>(name = paymentMethodType) as CardPaymentDelegate,
            cardDataMetadataRetriever = resolve(),
            cardDataMetadataStateRetriever = resolve(),
            cardInputDataValidator = resolve(name = paymentMethodType),
            sdkAnalyticsEventLoggingDelegate = resolve(name = paymentMethodType),
            mockConfigurationDelegate = resolve()
        )
    }
}
