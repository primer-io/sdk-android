package io.primer.android.ipay88.implementation.composer.ui.navigation.provider

import io.primer.android.ipay88.implementation.composer.ui.navigation.handler.IPay88NavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandlerFactory

internal class IPay88NavigatorProviderFactory : PaymentMethodNavigationHandlerFactory {
    override fun create(): PaymentMethodNavigationHandler {
        return IPay88NavigationHandler()
    }
}
