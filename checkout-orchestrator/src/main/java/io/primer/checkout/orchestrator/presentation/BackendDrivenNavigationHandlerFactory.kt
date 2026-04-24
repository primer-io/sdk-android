package io.primer.checkout.orchestrator.presentation

import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandlerFactory
import io.primer.checkout.orchestrator.domain.ui.StepUiHandlerRegistry

internal class BackendDrivenNavigationHandlerFactory : PaymentMethodNavigationHandlerFactory, DISdkComponent {
    override fun create(): PaymentMethodNavigationHandler {
        val registry: StepUiHandlerRegistry = resolve()
        return BackendDrivenNavigationHandler(registry.createNavigationHandlers())
    }
}
