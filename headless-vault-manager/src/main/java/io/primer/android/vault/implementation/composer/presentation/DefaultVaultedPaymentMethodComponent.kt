package io.primer.android.vault.implementation.composer.presentation

import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.core.composer.VaultedPaymentMethodComponent
import io.primer.android.payments.core.helpers.PaymentMethodPaymentDelegate

internal class DefaultVaultedPaymentMethodComponent(override val paymentDelegate: PaymentMethodPaymentDelegate) :
    VaultedPaymentMethodComponent {

    override fun start(paymentMethodType: String, sessionIntent: PrimerSessionIntent) {
        // no-op
    }

    override fun cancel() {
        // no-op
    }
}
