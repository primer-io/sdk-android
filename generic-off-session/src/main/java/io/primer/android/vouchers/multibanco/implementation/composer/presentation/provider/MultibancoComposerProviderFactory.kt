package io.primer.android.vouchers.multibanco.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.vouchers.multibanco.implementation.composer.MultibancoComponent
import io.primer.android.vouchers.multibanco.implementation.payment.delegate.MultibancoPaymentDelegate

internal class MultibancoComposerProviderFactory : PaymentMethodComposerProvider.Factory {
    override fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    ): PaymentMethodComposer {
        return MultibancoComponent(
            tokenizationDelegate = resolve(),
            paymentDelegate =
            MultibancoPaymentDelegate(
                paymentMethodTokenHandler = resolve(),
                resumePaymentHandler = resolve(),
                config = resolve(),
                pendingResumeHandler = resolve(),
                successHandler = resolve(),
                manualFlowSuccessHandler = resolve(),
                errorHandler = resolve(),
                baseErrorResolver = resolve(),
                resumeHandler = resolve(),
            ),
        )
    }
}
