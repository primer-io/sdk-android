package io.primer.android.googlepay.implementation.composer.ui.navigation.provider

import io.primer.android.core.di.DISdkComponent
import io.primer.android.googlepay.implementation.composer.ui.navigation.handler.GooglePayNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandlerFactory

internal class GooglePayNavigatorProviderFactory :
    PaymentMethodNavigationHandlerFactory, DISdkComponent {
    override fun create(): PaymentMethodNavigationHandler {
        return GooglePayNavigationHandler()
    }
}
