package io.primer.android.webRedirectShared.implementation.composer.ui.navigation.provider

import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandlerFactory
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.handler.WebRedirectNavigationHandler

class WebRedirectNavigatorProviderFactory :
    PaymentMethodNavigationHandlerFactory {
    override fun create(): PaymentMethodNavigationHandler {
        return WebRedirectNavigationHandler()
    }
}
