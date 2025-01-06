package io.primer.android.otp.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.otp.implementation.composer.presentation.OtpComponent
import io.primer.android.otp.implementation.payment.delegate.OtpPaymentDelegate
import io.primer.android.otp.implementation.tokenization.presentation.OtpTokenizationDelegate
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.payments.di.PaymentsContainer

internal class OtpComposerProviderFactory : PaymentMethodComposerProvider.Factory {
    override fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    ): PaymentMethodComposer {
        return OtpComponent(
            tokenizationDelegate =
                OtpTokenizationDelegate(
                    tokenizationInteractor = resolve(name = paymentMethodType),
                    configurationInteractor = resolve(name = paymentMethodType),
                ),
            pollingInteractor = resolve(PaymentsContainer.POLLING_INTERACTOR_DI_KEY),
            paymentDelegate =
                OtpPaymentDelegate(
                    paymentMethodTokenHandler = resolve(),
                    resumePaymentHandler = resolve(),
                    successHandler = resolve(),
                    errorHandler = resolve(),
                    pollingStartHandler = resolve(),
                    baseErrorResolver = resolve(),
                    resumeHandler = resolve(),
                    tokenizedPaymentMethodRepository = resolve(),
                ),
            pollingStartHandler = resolve(),
            collectableDataValidator = resolve(),
            sdkAnalyticsEventLoggingDelegate = resolve(name = paymentMethodType),
            errorMapperRegistry = resolve(),
        )
    }
}
