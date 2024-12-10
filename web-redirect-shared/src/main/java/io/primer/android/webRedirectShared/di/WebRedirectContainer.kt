package io.primer.android.webRedirectShared.di

import io.primer.android.webRedirectShared.implementation.composer.presentation.delegate.WebRedirectDelegate
import io.primer.android.webRedirectShared.implementation.composer.presentation.delegate.WebRedirectLoggingDelegate
import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.webRedirectShared.implementation.composer.presentation.viewmodel.WebRedirectViewModelFactory

class WebRedirectContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory {
            WebRedirectViewModelFactory(
                analyticsInteractor = sdk().resolve()
            )
        }
        registerSingleton {
            WebRedirectDelegate(
                errorHandler = sdk().resolve(),
                successHandler = sdk().resolve()
            )
        }

        registerSingleton {
            WebRedirectLoggingDelegate(
                logReporter = sdk().resolve(),
                analyticsInteractor = sdk().resolve()
            )
        }
    }
}
