package io.primer.android.paymentmethods.core.ui.navigation

interface PaymentMethodNavigationHandlerFactory {
    fun create(): PaymentMethodNavigationHandler
}

interface PaymentMethodNavigationHandler
