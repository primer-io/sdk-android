package io.primer.android.di

import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.data.deeplink.async.AsyncPaymentMethodDeeplinkDataRepository
import io.primer.android.domain.deeplink.async.repository.AsyncPaymentMethodDeeplinkRepository
import io.primer.android.domain.payments.helpers.ResumeEventResolver
import io.primer.android.logging.DefaultLogger
import io.primer.android.logging.Logger

internal class ResumeEventContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton<AsyncPaymentMethodDeeplinkRepository> {
            AsyncPaymentMethodDeeplinkDataRepository(
                sdk.resolve()
            )
        }

        registerFactory<Logger>(RESUME_HANDLER_LOGGER_NAME) {
            DefaultLogger(
                RESUME_HANDLER_LOGGER_NAME
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
                resolve(RESUME_HANDLER_LOGGER_NAME),
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

    companion object {
        internal const val RESUME_HANDLER_LOGGER_NAME = "RESUME_HANDLER"
    }
}
