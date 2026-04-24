package io.primer.checkout.orchestrator.domain.ui

import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.provider.WebRedirectNavigatorProviderFactory
import io.primer.paymentMethodCoreUi.core.ui.navigation.PaymentMethodContextNavigationHandler

internal class UrlOpenStepUiHandlerFactory : StepUiHandlerFactory, DISdkComponent {
    override fun create(): StepUiHandler = UrlOpenStepUiHandler(
        urlOpenHandler = resolve(),
        returnUriProvider = resolve(),
    )

    override fun createNavigationHandler(): PaymentMethodContextNavigationHandler =
        WebRedirectNavigatorProviderFactory().create() as PaymentMethodContextNavigationHandler
}
