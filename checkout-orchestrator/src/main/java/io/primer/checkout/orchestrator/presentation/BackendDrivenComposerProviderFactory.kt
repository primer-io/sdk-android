package io.primer.checkout.orchestrator.presentation

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.composer.PaymentMethodComposer
import io.primer.android.paymentmethods.core.composer.provider.PaymentMethodComposerProvider
import io.primer.checkout.orchestrator.domain.ui.StepUiHandlerRegistry

class BackendDrivenComposerProviderFactory :
    PaymentMethodComposerProvider.Factory,
    DISdkComponent {

    override fun create(
        paymentMethodType: String,
        sessionIntent: PrimerSessionIntent,
    ): PaymentMethodComposer {
        val registry: StepUiHandlerRegistry = resolve()
        return BackendDrivenCheckoutComponent(
            paymentFlowInteractor = resolve(),
            preTokenizationHandler = resolve(),
            successHandler = resolve(),
            errorHandler = resolve(),
            checkoutDecisionResolver = resolve(),
            returnUriProvider = resolve(),
            baseErrorResolver = resolve(),
            stepUiHandlers = registry.createHandlers(),
        )
    }
}
