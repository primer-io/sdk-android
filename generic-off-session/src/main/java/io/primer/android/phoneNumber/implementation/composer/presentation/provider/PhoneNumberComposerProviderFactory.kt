package io.primer.android.phoneNumber.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.payments.di.PaymentsContainer
import io.primer.android.phoneNumber.implementation.composer.presentation.PhoneNumberComponent
import io.primer.android.phoneNumber.implementation.payment.delegate.PhoneNumberPaymentDelegate
import io.primer.android.phoneNumber.implementation.tokenization.presentation.PhoneNumberTokenizationDelegate

internal class PhoneNumberComposerProviderFactory : PaymentMethodComposerProvider.Factory {

    override fun create(paymentMethodType: String, sessionIntent: PrimerSessionIntent): PaymentMethodComposer {
        return PhoneNumberComponent(
            tokenizationDelegate = PhoneNumberTokenizationDelegate(
                tokenizationInteractor = resolve(name = paymentMethodType),
                configurationInteractor = resolve(name = paymentMethodType)
            ),
            pollingInteractor = resolve(PaymentsContainer.POLLING_INTERACTOR_DI_KEY),
            paymentDelegate = PhoneNumberPaymentDelegate(
                paymentMethodTokenHandler = resolve(),
                resumePaymentHandler = resolve(),
                successHandler = resolve(),
                errorHandler = resolve(),
                pollingStartHandler = resolve(),
                baseErrorResolver = resolve(),
                resumeHandler = resolve(),
                tokenizedPaymentMethodRepository = resolve()
            ),
            pollingStartHandler = resolve(),
            collectableDataValidator = resolve(),
            sdkAnalyticsEventLoggingDelegate = resolve(name = paymentMethodType),
            errorMapperRegistry = resolve()
        )
    }
}
