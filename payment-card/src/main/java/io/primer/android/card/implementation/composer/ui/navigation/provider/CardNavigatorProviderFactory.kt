package io.primer.android.card.implementation.composer.ui.navigation.provider

import io.primer.android.card.implementation.composer.ui.navigation.handler.CardNavigationHandler
import io.primer.android.core.di.DISdkComponent
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandler
import io.primer.android.paymentmethods.core.ui.navigation.PaymentMethodNavigationHandlerFactory

internal class CardNavigatorProviderFactory :
    PaymentMethodNavigationHandlerFactory, DISdkComponent {
    override fun create(): PaymentMethodNavigationHandler {
        return CardNavigationHandler()
    }
}
