package io.primer.android.paypal.implementation.composer.ui.navigation.provider

import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandlerFactory
import io.primer.android.paypal.implementation.composer.ui.navigation.handler.PaypalNavigationHandler

internal class PaypalNavigatorProviderFactory : PaymentMethodNavigationHandlerFactory {
    override fun create(): PaymentMethodNavigationHandler {
        return PaypalNavigationHandler()
    }
}
