package io.primer.android.banks.implementation.composer.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.banks.di.BankWebRedirectComposer
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider

internal class BankIssuerComposerProviderFactory : PaymentMethodComposerProvider.Factory {
    override fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    ): PaymentMethodComposer {
        return resolve<BankWebRedirectComposer>(name = paymentMethodType)
    }
}
