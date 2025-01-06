package io.primer.android.qrcode.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.payments.di.PaymentsContainer
import io.primer.android.qrcode.implementation.composer.presentation.QrCodeComponent
import io.primer.android.qrcode.implementation.payment.delegate.QrCodePaymentDelegate

internal class QrCodeComposerProviderFactory : PaymentMethodComposerProvider.Factory {
    override fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    ): PaymentMethodComposer {
        return QrCodeComponent(
            tokenizationDelegate = resolve(),
            pollingInteractor = resolve(PaymentsContainer.POLLING_INTERACTOR_DI_KEY),
            paymentDelegate =
                QrCodePaymentDelegate(
                    paymentMethodTokenHandler = resolve(),
                    resumePaymentHandler = resolve(),
                    successHandler = resolve(),
                    pollingStartHandler = resolve(),
                    additionalInfoHandler = resolve(),
                    errorHandler = resolve(),
                    baseErrorResolver = resolve(),
                    resumeHandler = resolve(),
                    tokenizedPaymentMethodRepository = resolve(),
                ),
            pollingStartHandler = resolve(),
        )
    }
}
