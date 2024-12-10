package io.primer.android.vouchers.retailOutlets.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.android.vouchers.retailOutlets.implementation.composer.RetailOutletsComponent
import io.primer.android.vouchers.retailOutlets.implementation.payment.delegate.RetailOutletsPaymentDelegate

internal class RetailOutletsComposerProviderFactory : PaymentMethodComposerProvider.Factory {

    override fun create(paymentMethodType: String, sessionIntent: PrimerSessionIntent): PaymentMethodComposer {
        return RetailOutletsComponent(
            tokenizationDelegate = resolve(),
            paymentDelegate = RetailOutletsPaymentDelegate(
                paymentMethodTokenHandler = resolve(),
                resumePaymentHandler = resolve(),
                successHandler = resolve(),
                errorHandler = resolve(),
                baseErrorResolver = resolve(),
                resumeHandler = resolve()
            ),
            retailOutletsDataValidator = resolve(name = paymentMethodType),
            retailOutletInteractor = resolve(name = paymentMethodType),
            errorMapperRegistry = resolve()
        )
    }
}
