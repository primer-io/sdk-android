package io.primer.android.googlepay.implementation.composer.presentation.provider

import io.primer.android.PrimerSessionIntent
import io.primer.android.clientSessionActions.di.ActionsContainer
import io.primer.android.core.di.extensions.resolve
import io.primer.android.googlepay.implementation.composer.GooglePayComponent
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider

internal class GooglePayComposerProviderFactory : PaymentMethodComposerProvider.Factory {
    override fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    ): PaymentMethodComposer {
        return GooglePayComponent(
            tokenizationCollectorDelegate = resolve(name = paymentMethodType),
            tokenizationDelegate = resolve(name = paymentMethodType),
            actionInteractor = resolve(ActionsContainer.ACTION_INTERACTOR_DI_KEY),
            shippingMethodUpdateValidator = resolve(),
            validationRulesResolver = resolve(),
            paymentDelegate = resolve(name = paymentMethodType),
            mockConfigurationDelegate = resolve(),
        )
    }
}
