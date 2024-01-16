package io.primer.android.di

import io.primer.android.components.presentation.paymentMethods.formWithRedirect.redirect.webRedirect.delegate.WebRedirectDelegate
import io.primer.android.components.presentation.paymentMethods.formWithRedirect.redirect.webRedirect.delegate.WebRedirectLoggingDelegate

internal class WebRedirectContainer(private val sdk: SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton { WebRedirectDelegate() }

        registerSingleton {
            WebRedirectLoggingDelegate(
                logReporter = sdk.resolve(),
                analyticsInteractor = sdk.resolve()
            )
        }
    }
}
