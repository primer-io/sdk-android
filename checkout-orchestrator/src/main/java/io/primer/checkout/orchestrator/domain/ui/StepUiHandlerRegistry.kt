package io.primer.checkout.orchestrator.domain.ui

import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler

class StepUiHandlerRegistry {
    private val factories = mutableListOf<StepUiHandlerFactory>()

    fun register(factory: StepUiHandlerFactory) {
        factories.add(factory)
    }

    fun createHandlers(): List<StepUiHandler> = factories.map { it.create() }

    fun createNavigationHandlers(): List<PaymentMethodContextNavigationHandler> =
        factories.mapNotNull { it.createNavigationHandler() }
}
