package io.primer.checkout.orchestrator.domain.ui

import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler

/**
 * Factory for creating a [StepUiHandler] and its associated navigation handler.
 * Each module that introduces a new step type implements this interface
 * and registers it with [StepUiHandlerRegistry].
 */
interface StepUiHandlerFactory {
    fun create(): StepUiHandler

    fun createNavigationHandler(): PaymentMethodContextNavigationHandler? = null
}
