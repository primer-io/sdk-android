package io.primer.android.di

import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.deeplink.async.AsyncPaymentMethodDeeplinkDataRepository
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.payments.helpers.ResumeEventResolver

internal class ResumeEventContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<AsyncPaymentMethodDeeplinkRepository> {
            AsyncPaymentMethodDeeplinkDataRepository(
                sdk.resolve()
            )
        }

        registerFactory {
            ResumeHandlerFactory(
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve(),
                resolve()
            )
        }

        registerFactory {
            ResumeEventResolver(
                sdk.resolve(),
                resolve(),
                sdk.resolve()
            )
        }
    }
}
