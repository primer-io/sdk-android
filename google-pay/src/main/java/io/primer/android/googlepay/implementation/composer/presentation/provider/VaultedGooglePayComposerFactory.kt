package io.primer.android.googlepay.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.googlepay.implementation.composer.VaultedGooglePayComponent
import io.primer.android.googlepay.implementation.payment.delegate.GooglePayPaymentDelegate
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider

internal class VaultedGooglePayComposerFactory : PaymentMethodComposerProvider.Factory {

    override fun create(paymentMethodType: String, sessionIntent: PrimerSessionIntent): PaymentMethodComposer {
        return VaultedGooglePayComponent(
            paymentDelegate = resolve<GooglePayPaymentDelegate>(name = paymentMethodType)
        )
    }
}
