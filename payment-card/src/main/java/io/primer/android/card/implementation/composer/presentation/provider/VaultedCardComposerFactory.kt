package io.primer.android.card.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.card.implementation.composer.VaultedCardComponent
import io.primer.android.card.implementation.payment.delegate.CardPaymentDelegate
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate

internal class VaultedCardComposerFactory : PaymentMethodComposerProvider.Factory {
    override fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    ): PaymentMethodComposer {
        return VaultedCardComponent(
            paymentDelegate = resolve<PaymentMethodPaymentDelegate>(name = paymentMethodType) as CardPaymentDelegate,
        )
    }
}
